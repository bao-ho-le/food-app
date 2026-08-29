package com.example.foodie.ordering.order.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.OrderingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// BVA quanh biên "id hợp lệ phải là số nguyên dương". 0 và 1 là hai biên quan trọng
// nhất vì chúng bắt mutation <= <-> <.
class OrderHelperValidationTest {

    private final OrderHelper orderHelper = new OrderHelper();

    @Test
    @DisplayName("validateOrderId(null) ném ORDER_ID_REQUIRED")
    void should_throwOrderIdRequired_when_orderIdIsNull() {
        assertThatThrownBy(() -> orderHelper.validateOrderId(null))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_ID_REQUIRED);
    }

    @Test
    @DisplayName("validateOrderId(-1) và validateOrderId(0) đều ném ORDER_ID_INVALID")
    void should_throwOrderIdInvalid_when_orderIdIsNotPositive() {
        assertThatThrownBy(() -> orderHelper.validateOrderId(-1))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_ID_INVALID);
        assertThatThrownBy(() -> orderHelper.validateOrderId(0))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_ID_INVALID);
    }

    @Test
    @DisplayName("validateOrderId(1) không ném gì — biên dưới hợp lệ")
    void should_notThrow_when_orderIdIsOne() {
        assertThatCode(() -> orderHelper.validateOrderId(1)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAddressId(null) ném ADDRESS_ID_REQUIRED")
    void should_throwAddressIdRequired_when_addressIdIsNull() {
        assertThatThrownBy(() -> orderHelper.validateAddressId(null))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_ID_REQUIRED);
    }

    @Test
    @DisplayName("validateAddressId(-1) và validateAddressId(0) đều ném ADDRESS_ID_INVALID")
    void should_throwAddressIdInvalid_when_addressIdIsNotPositive() {
        assertThatThrownBy(() -> orderHelper.validateAddressId(-1))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_ID_INVALID);
        assertThatThrownBy(() -> orderHelper.validateAddressId(0))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_ID_INVALID);
    }

    @Test
    @DisplayName("validateAddressId(1) không ném gì — biên dưới hợp lệ")
    void should_notThrow_when_addressIdIsOne() {
        assertThatCode(() -> orderHelper.validateAddressId(1)).doesNotThrowAnyException();
    }
}
