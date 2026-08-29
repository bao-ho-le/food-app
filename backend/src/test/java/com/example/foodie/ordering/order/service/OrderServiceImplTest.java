package com.example.foodie.ordering.order.service;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.catalog.image.repository.ImageRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.repository.AddressRepository;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.ordering.order.helper.OrderHelper;
import com.example.foodie.ordering.order.mapper.OrderMapper;
import com.example.foodie.ordering.order.repository.OrderDishRepository;
import com.example.foodie.ordering.order.repository.OrderRepository;
import com.example.foodie.ordering.userdish.entity.UserDish;
import com.example.foodie.ordering.userdish.repository.UserDishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Chỉ mock repository (Quy tắc 1) -- OrderHelper/OrderMapper là bản thật, UserHelper thật
// nhưng bọc UserRepository mock. Điều này đảm bảo test bắt được lỗi thật trong logic
// nghiệp vụ của OrderServiceImpl thay vì chỉ xác nhận "có gọi helper".
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserDishRepository userDishRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderDishRepository orderDishRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;

    private OrderServiceImpl orderService;
    private User caller;

    @BeforeEach
    void setUp() {
        caller = User.builder().id(1).email("caller@test.local").build();
        UserHelper userHelper = new UserHelper(userRepository);
        orderService = new OrderServiceImpl(orderRepository, userDishRepository, addressRepository,
                orderDishRepository, imageRepository, dishRepository, new OrderHelper(), new OrderMapper(), userHelper);
    }

    private void stubCaller() {
        when(authentication.getName()).thenReturn("caller@test.local");
        when(userRepository.findByEmail("caller@test.local")).thenReturn(Optional.of(caller));
    }

    private static Dish dish(int id, float price, int stock, boolean available) {
        return Dish.builder().id(id).name("Dish " + id).price(price).stockQuantity(stock).isAvailable(available).build();
    }

    private static UserDish cartItem(int id, Dish dish, int quantity) {
        return UserDish.builder().id(id).dish(dish).quantity(quantity).build();
    }

    private void stubLock(Dish... dishes) {
        for (Dish d : dishes) {
            when(dishRepository.findByIdForUpdate(d.getId())).thenReturn(Optional.of(d));
        }
    }

    private void stubOrderSaveEchoesArgument() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    class TaoDonHang {

        @Test
        @DisplayName("Giỏ hàng rỗng ném ORDER_CART_EMPTY")
        void should_throwCartEmpty_when_cartIsEmpty() {
            stubCaller();
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(List.of());

            assertThatThrownBy(() -> orderService.createOrder(authentication, 1))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ORDER_CART_EMPTY);
        }

        @Test
        @DisplayName("Địa chỉ không tồn tại (hoặc không thuộc người gọi) ném ADDRESS_NOT_FOUND")
        void should_throwAddressNotFound_when_addressDoesNotBelongToCaller() {
            stubCaller();
            Dish dish = dish(1, 30_000f, 10, true);
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(List.of(cartItem(1, dish, 1)));
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(authentication, 1))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // "Order nào được lưu" theo nghĩa mock-observable ở Phase 2 KHÔNG bao gồm
        // orderRepository.save(): OrderServiceImpl lưu một "order shell" rỗng TRƯỚC khi
        // buildOrderDishes kiểm dish, nên save() vẫn được gọi một lần dù sau đó ném lỗi --
        // việc order đó có thực sự tồn tại trong DB hay không phụ thuộc @Transactional
        // rollback (Phase 5, Quy tắc 4). Ở đây ta kiểm 3 điều mock THẬT SỰ chứng minh được:
        // không món nào bị trừ kho, không orderDish nào được tạo, giỏ hàng không bị xoá.
        @Test
        @DisplayName("Giỏ 2 món, món 2 not available -> DISH_NOT_AVAILABLE, không trừ kho, không tạo orderDish, không xoá giỏ")
        void should_throwDishNotAvailable_andRollBackNothingObservable_when_secondDishUnavailable() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 10, true);
            Dish dishB = dish(2, 65_000f, 5, false);
            List<UserDish> cart = List.of(cartItem(1, dishA, 1), cartItem(2, dishB, 1));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA, dishB);
            stubOrderSaveEchoesArgument();

            assertThatThrownBy(() -> orderService.createOrder(authentication, 1))
                    .isInstanceOf(CatalogException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DISH_NOT_AVAILABLE);

            verify(dishRepository, never()).saveAll(anyList());
            verify(orderDishRepository, never()).saveAll(anyList());
            verify(userDishRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("Giỏ 2 món, món 2 tồn kho 1 < số lượng đặt 3 -> DISH_OUT_OF_STOCK")
        void should_throwOutOfStock_when_secondDishStockInsufficient() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 10, true);
            Dish dishB = dish(2, 65_000f, 1, true);
            List<UserDish> cart = List.of(cartItem(1, dishA, 1), cartItem(2, dishB, 3));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA, dishB);
            stubOrderSaveEchoesArgument();

            assertThatThrownBy(() -> orderService.createOrder(authentication, 1))
                    .isInstanceOf(CatalogException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DISH_OUT_OF_STOCK);

            verify(dishRepository, never()).saveAll(anyList());
            verify(orderDishRepository, never()).saveAll(anyList());
        }

        // Bắt mutation "quên nhân số lượng": nếu code chỉ cộng đơn giá mà bỏ qua quantity,
        // kết quả sẽ là 95_000 thay vì 125_000.
        @Test
        @DisplayName("Giỏ: món A (30k x2) + món B (65k x1) -> totalPrice = 125_000")
        void should_computeTotalPriceMultipliedByQuantity() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 10, true);
            Dish dishB = dish(2, 65_000f, 5, true);
            List<UserDish> cart = List.of(cartItem(1, dishA, 2), cartItem(2, dishB, 1));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA, dishB);
            stubOrderSaveEchoesArgument();

            orderService.createOrder(authentication, 1);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getTotalPrice()).isEqualTo(125_000f);
        }

        @Test
        @DisplayName("Sau khi đặt, tồn kho A và B đều bị trừ đúng số lượng")
        void should_deductStock_forEachOrderedDish() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 10, true);
            Dish dishB = dish(2, 65_000f, 5, true);
            List<UserDish> cart = List.of(cartItem(1, dishA, 2), cartItem(2, dishB, 1));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA, dishB);
            stubOrderSaveEchoesArgument();

            orderService.createOrder(authentication, 1);

            // dishA/dishB là entity thật được chính service mutate qua tham chiếu trả về
            // từ findByIdForUpdate -- assert trực tiếp trạng thái, không cần ArgumentCaptor.
            assertThat(dishA.getStockQuantity()).isEqualTo(8);
            assertThat(dishB.getStockQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("Sau khi đặt, toàn bộ mục giỏ hàng bị xoá")
        void should_deleteAllCartItems_afterOrderCreated() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 10, true);
            List<UserDish> cart = List.of(cartItem(1, dishA, 2));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA);
            stubOrderSaveEchoesArgument();

            orderService.createOrder(authentication, 1);

            ArgumentCaptor<List<UserDish>> captor = ArgumentCaptor.forClass(List.class);
            verify(userDishRepository).deleteAll(captor.capture());
            assertThat(captor.getValue()).isEqualTo(cart);
        }

        @Test
        @DisplayName("Món A kho=5, đặt đúng 5 -> thành công, kho về 0 (biên qty == stock)")
        void should_succeed_when_orderedQuantityEqualsStock() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 5, true);
            List<UserDish> cart = List.of(cartItem(1, dishA, 5));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA);
            stubOrderSaveEchoesArgument();

            orderService.createOrder(authentication, 1);

            assertThat(dishA.getStockQuantity()).isZero();
        }

        @Test
        @DisplayName("Món A kho=5, đặt 6 -> DISH_OUT_OF_STOCK (biên qty == stock + 1)")
        void should_throwOutOfStock_when_orderedQuantityExceedsStockByOne() {
            stubCaller();
            Address address = Address.builder().id(1).user(caller).address("123 Le Loi").build();
            Dish dishA = dish(1, 30_000f, 5, true);
            List<UserDish> cart = List.of(cartItem(1, dishA, 6));
            when(userDishRepository.findAllByUser_Id(1)).thenReturn(cart);
            when(addressRepository.findByIdAndUser_Id(1, 1)).thenReturn(Optional.of(address));
            stubLock(dishA);
            stubOrderSaveEchoesArgument();

            assertThatThrownBy(() -> orderService.createOrder(authentication, 1))
                    .isInstanceOf(CatalogException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DISH_OUT_OF_STOCK);
        }
    }

    @Nested
    class HuyVaXacNhanDonHang {

        @Test
        @DisplayName("Huỷ đơn PENDING của chính người gọi -> CANCELLED, hoàn kho đầy đủ")
        void should_cancelAndRestoreStock_when_pendingOrderCancelledByOwner() {
            stubCaller();
            Dish dishA = dish(1, 30_000f, 7, true);
            Order order = Order.builder().id(10).user(caller).status(Status.PENDING).totalPrice(90_000f).build();
            OrderDish orderDish = OrderDish.builder().id(1).order(order).dish(dishA).quantity(3).price(30_000f).build();
            when(orderRepository.findById(10)).thenReturn(Optional.of(order));
            when(orderDishRepository.findByOrder_Id(10)).thenReturn(List.of(orderDish));
            stubLock(dishA);
            when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDTO result = orderService.cancelOwnOrder(authentication, 10);

            assertThat(result.getStatus()).isEqualTo(Status.CANCELLED);
            assertThat(dishA.getStockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("Huỷ đơn thuộc người khác ném ORDER_NOT_OWNER")
        void should_throwNotOwner_when_orderBelongsToAnotherUser() {
            stubCaller();
            User otherUser = User.builder().id(2).build();
            Order order = Order.builder().id(10).user(otherUser).status(Status.PENDING).build();
            when(orderRepository.findById(10)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOwnOrder(authentication, 10))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_OWNER);
        }

        @Test
        @DisplayName("Huỷ đơn đang DELIVERING ném ORDER_INVALID_STATUS_TRANSITION, kho không đổi")
        void should_throwInvalidTransition_when_cancellingDeliveringOrder() {
            stubCaller();
            Order order = Order.builder().id(10).user(caller).status(Status.DELIVERING).build();
            when(orderRepository.findById(10)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOwnOrder(authentication, 10))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);

            verify(dishRepository, never()).saveAll(anyList());
            verify(orderDishRepository, never()).findByOrder_Id(any());
        }

        // Chứng minh phép dịch exception (Quy tắc 4): saveAndFlush ném optimistic lock
        // failure -> service đổi thành ORDER_STATUS_CONFLICT. KHÔNG chứng minh @Version
        // thực sự hoạt động ở tầng DB -- đó thuộc Phase 6.
        @Test
        @DisplayName("saveAndFlush ném ObjectOptimisticLockingFailureException -> ORDER_STATUS_CONFLICT")
        void should_translateOptimisticLockFailure_toStatusConflict() {
            Order order = Order.builder().id(10).status(Status.PENDING).build();
            when(orderRepository.findById(10)).thenReturn(Optional.of(order));
            when(orderRepository.saveAndFlush(any(Order.class)))
                    .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 10));

            assertThatThrownBy(() -> orderService.updateStatus(10, Status.PREPARING))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ORDER_STATUS_CONFLICT);
        }

        @Test
        @DisplayName("Xác nhận đã nhận đơn DELIVERING của chính mình -> DELIVERED")
        void should_markDelivered_when_ownerConfirmsDeliveringOrderReceived() {
            stubCaller();
            Order order = Order.builder().id(10).user(caller).status(Status.DELIVERING).build();
            when(orderRepository.findById(10)).thenReturn(Optional.of(order));
            when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponseDTO result = orderService.confirmOwnOrderReceived(authentication, 10);

            assertThat(result.getStatus()).isEqualTo(Status.DELIVERED);
        }
    }

    @Nested
    class XoaDonHang {

        // Đơn đã giao là dữ liệu kế toán -- phải giữ lại cho audit/reporting.
        @Test
        @DisplayName("Xoá đơn DELIVERED ném ORDER_DELETE_FORBIDDEN, không gọi deleteById")
        void should_throwDeleteForbidden_when_orderIsDelivered() {
            Order order = Order.builder().id(10).status(Status.DELIVERED).build();
            when(orderRepository.findById(10)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.deleteById(10))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ORDER_DELETE_FORBIDDEN);

            verify(orderRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Xoá đơn không tồn tại ném ORDER_NOT_FOUND")
        void should_throwOrderNotFound_when_orderDoesNotExist() {
            when(orderRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.deleteById(99))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        }
    }
}
