package com.example.foodie.identity.admin.dashboard.service;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.service.DishService;
import com.example.foodie.catalog.restaurant.repository.RestaurantRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardStatsDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardTrendPointDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.TopProductDTO;
import com.example.foodie.identity.admin.dashboard.mapper.DashboardMapper;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.ordering.order.repository.OrderDishRepository;
import com.example.foodie.ordering.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Service gọi Instant.now()/LocalDate.now() trực tiếp (không tiêm được thời gian), nên
// mọi assert ở đây dựa trên CẤU TRÚC (số điểm, tổng dồn, thứ tự) chứ không pin mốc ngày
// cứng -- pin ngày sẽ khiến test vỡ vào nửa đêm hoặc ở UTC offset khác.
//
// DishService là một Service collaborator (không phải repository, không phải helper/mapper
// "không dependency" theo Quy tắc 1), dựng bản thật sẽ kéo theo toàn bộ repository của
// DishServiceImpl -- mock nó như một biên ngoài của DashboardServiceImpl.
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderDishRepository orderDishRepository;
    @Mock
    private DishService dishService;

    private final DashboardMapper dashboardMapper = new DashboardMapper();

    private DashboardServiceImpl dashboardService() {
        return new DashboardServiceImpl(orderRepository, userRepository, restaurantRepository,
                orderDishRepository, dishService, dashboardMapper);
    }

    @Test
    @DisplayName("getRevenueTrend(5) ném DASHBOARD_INVALID_TREND_PERIOD -- chỉ chấp nhận 7/14/30")
    void should_throwInvalidTrendPeriod_when_daysIsNotAllowed() {
        assertThatThrownBy(() -> dashboardService().getRevenueTrend(5))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DASHBOARD_INVALID_TREND_PERIOD);
    }

    @Test
    @DisplayName("getRevenueTrend(7) không có đơn nào -> đúng 7 điểm liên tiếp, mọi giá trị bằng 0")
    void should_returnSevenConsecutiveZeroPoints_when_noOrdersInRange() {
        when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
        when(userRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        List<DashboardTrendPointDTO> trend = dashboardService().getRevenueTrend(7);

        assertThat(trend).hasSize(7);
        assertThat(trend).allSatisfy(point -> {
            assertThat(point.getRevenue()).isZero();
            assertThat(point.getOrderCount()).isZero();
            assertThat(point.getNewCustomerCount()).isZero();
        });
        for (int i = 1; i < trend.size(); i++) {
            LocalDate previous = LocalDate.parse(trend.get(i - 1).getDate());
            LocalDate current = LocalDate.parse(trend.get(i).getDate());
            assertThat(current).isEqualTo(previous.plusDays(1));
        }
    }

    @Test
    @DisplayName("1 đơn DELIVERED 100k và 1 đơn PENDING 200k -> doanh thu 100k, đếm đơn cả hai (2)")
    void should_onlyCountDeliveredRevenue_butCountAllStatusesForOrderCount() {
        Instant now = Instant.now();
        Order delivered = Order.builder().id(1).status(Status.DELIVERED).totalPrice(100_000f).createdAt(now).build();
        Order pending = Order.builder().id(2).status(Status.PENDING).totalPrice(200_000f).createdAt(now).build();
        when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(delivered, pending));
        when(userRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        List<DashboardTrendPointDTO> trend = dashboardService().getRevenueTrend(7);

        double totalRevenue = trend.stream().mapToDouble(DashboardTrendPointDTO::getRevenue).sum();
        long totalOrderCount = trend.stream().mapToLong(DashboardTrendPointDTO::getOrderCount).sum();
        assertThat(totalRevenue).isEqualTo(100_000.0);
        assertThat(totalOrderCount).isEqualTo(2);
    }

    @Test
    @DisplayName("previous=0, current=0 -> phần trăm thay đổi = 0.0")
    void should_returnZeroPercentChange_when_bothCurrentAndPreviousAreZero() {
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L, 0L);

        DashboardStatsDTO stats = dashboardService().getDashboardStats();

        assertThat(stats.getTotalUsersChangePercent()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("previous=0, current>0 -> phần trăm thay đổi = 100.0")
    void should_returnHundredPercentChange_when_previousIsZeroAndCurrentIsPositive() {
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L, 0L);

        DashboardStatsDTO stats = dashboardService().getDashboardStats();

        assertThat(stats.getTotalUsersChangePercent()).isEqualTo(100.0);
    }

    // Bug thật kiểu này từng tồn tại: break dừng đúng ở 5 dòng đầu của kết quả truy vấn,
    // bất kể dish ở dòng đó còn tồn tại trong danh mục hay không -- nên nếu dish bị xoá
    // rơi vào top 5 ban đầu, kết quả trả về ÍT HƠN 5 (không bù thêm từ các dòng còn lại).
    @Test
    @DisplayName("8 món bán chạy, 1 dishId không còn trong danh mục -> bỏ qua, không NPE, tối đa 5 món")
    void should_skipDishNotInCatalog_withoutThrowingNpe() {
        List<Object[]> rows = List.of(
                new Object[]{1, 50L}, new Object[]{2, 40L}, new Object[]{3, 30L},
                new Object[]{4, 20L}, new Object[]{5, 10L}, new Object[]{6, 9L},
                new Object[]{7, 8L}, new Object[]{8, 7L}
        );
        when(orderDishRepository.sumQuantityByDishForOrderStatus(Status.DELIVERED)).thenReturn(rows);
        // dishId=3 nằm trong 5 dòng đầu nhưng KHÔNG có trong danh mục trả về từ DishService
        when(dishService.getAllDishes()).thenReturn(List.of(
                dish(1), dish(2), dish(4), dish(5), dish(6), dish(7), dish(8)
        ));

        List<TopProductDTO> topProducts = dashboardService().getTopProducts();

        assertThat(topProducts).hasSizeLessThanOrEqualTo(5);
        assertThat(topProducts).extracting(TopProductDTO::getDishId).doesNotContain(3);
    }

    private static DishDTO dish(int id) {
        return DishDTO.builder().id(id).name("Dish " + id).price(10_000f).url("").build();
    }
}
