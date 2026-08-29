package com.example.foodie.ordering.order.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.ordering.order.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// State Transition (0-switch) coverage cho máy trạng thái đơn hàng.
// Nguồn dữ liệu duyệt Status.values() x Status.values() rồi lọc theo tập hợp lệ,
// nên nếu ai đó thêm một Status mới vào enum thì test tự động phủ luôn các ô mới.
class OrderStatusTransitionTest {

    private final OrderHelper orderHelper = new OrderHelper();

    // Tập chuyển hợp lệ theo đặc tả: luồng thuận PENDING->PREPARING->DELIVERING->DELIVERED,
    // huỷ chỉ được phép từ PENDING hoặc PREPARING (chưa giao cho shipper).
    private static final Map<Status, Set<Status>> VALID_TRANSITIONS = new EnumMap<>(Status.class);
    static {
        VALID_TRANSITIONS.put(Status.PENDING, EnumSet.of(Status.PREPARING, Status.CANCELLED));
        VALID_TRANSITIONS.put(Status.PREPARING, EnumSet.of(Status.DELIVERING, Status.CANCELLED));
        VALID_TRANSITIONS.put(Status.DELIVERING, EnumSet.of(Status.DELIVERED));
        VALID_TRANSITIONS.put(Status.DELIVERED, EnumSet.noneOf(Status.class));
        VALID_TRANSITIONS.put(Status.CANCELLED, EnumSet.noneOf(Status.class));
    }

    private static boolean isValid(Status current, Status next) {
        return VALID_TRANSITIONS.get(current).contains(next);
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(Status.values())
                .flatMap(current -> Stream.of(Status.values())
                        .filter(next -> isValid(current, next))
                        .map(next -> Arguments.of(current, next)));
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(Status.values())
                .flatMap(current -> Stream.of(Status.values())
                        .filter(next -> !isValid(current, next))
                        .map(next -> Arguments.of(current, next)));
    }

    @ParameterizedTest(name = "{0} -> {1} là hợp lệ")
    @MethodSource("validTransitions")
    @DisplayName("Cho phép đúng 5 chuyển trạng thái nằm trên luồng thuận hoặc luồng huỷ trước khi giao")
    void should_notThrow_when_transitionIsOnHappyPathOrEarlyCancel(Status current, Status next) {
        assertThatCode(() -> orderHelper.validateStatusTransition(current, next))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} -> {1} phải bị chặn")
    @MethodSource("invalidTransitions")
    @DisplayName("Chặn mọi chuyển trạng thái nhảy cóc, lùi, giữ nguyên, hoặc huỷ sau khi đã giao cho shipper")
    void should_throwOrderInvalidStatusTransition_when_transitionIsNotAllowed(Status current, Status next) {
        assertThatThrownBy(() -> orderHelper.validateStatusTransition(current, next))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
    }
}
