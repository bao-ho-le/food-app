package com.example.foodie.ordering.userdish.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Cùng khái niệm "số lượng" mang hai ý nghĩa khác nhau tuỳ thao tác:
// - Thêm món vào giỏ (validateUserDishRequest): quantity <= 0 là lỗi (thêm 0 món vô nghĩa).
// - Cập nhật số lượng (validateQuantity): quantity <= 0 là tín hiệu XOÁ món khỏi giỏ,
//   hợp lệ, không được chặn. Chỉ null mới là lỗi.
// Item 2 và 4 dưới đây đứng cạnh nhau có chủ ý — là bằng chứng sống sự bất đối xứng
// này là cố ý, không phải bug.
class UserDishHelperTest {

    private final UserDishHelper userDishHelper = new UserDishHelper();

    private static UserDishDTO dto(Integer quantity, Integer dishId) {
        UserDishDTO dto = new UserDishDTO();
        dto.setQuantity(quantity);
        dto.setDishId(dishId);
        return dto;
    }

    @Test
    @DisplayName("validateQuantity(null) ném USERDISH_QUANTITY_REQUIRED")
    void should_throwQuantityRequired_when_quantityIsNull() {
        assertThatThrownBy(() -> userDishHelper.validateQuantity(null))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERDISH_QUANTITY_REQUIRED);
    }

    @Test
    @DisplayName("validateQuantity(0) và validateQuantity(-5) không ném — đây là tín hiệu xoá món khỏi giỏ")
    void should_notThrow_when_quantityIsZeroOrNegative() {
        assertThatCode(() -> userDishHelper.validateQuantity(0)).doesNotThrowAnyException();
        assertThatCode(() -> userDishHelper.validateQuantity(-5)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUserDishRequest với quantity=0 hoặc null ném USERDISH_QUANTITY_INVALID")
    void should_throwQuantityInvalid_when_addingZeroOrNullQuantity() {
        assertThatThrownBy(() -> userDishHelper.validateUserDishRequest(dto(0, 1)))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERDISH_QUANTITY_INVALID);
        assertThatThrownBy(() -> userDishHelper.validateUserDishRequest(dto(null, 1)))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERDISH_QUANTITY_INVALID);
    }

    @Test
    @DisplayName("validateUserDishRequest với quantity=1 không ném — biên dưới hợp lệ khi thêm vào giỏ")
    void should_notThrow_when_addingWithQuantityOne() {
        assertThatCode(() -> userDishHelper.validateUserDishRequest(dto(1, 1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUserDishRequest(null) ném USERDISH_REQUEST_REQUIRED")
    void should_throwRequestRequired_when_dtoIsNull() {
        assertThatThrownBy(() -> userDishHelper.validateUserDishRequest(null))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERDISH_REQUEST_REQUIRED);
    }

    @Test
    @DisplayName("validateUserDishRequest với dishId=0 ném DISH_ID_INVALID")
    void should_throwDishIdInvalid_when_dishIdIsZero() {
        assertThatThrownBy(() -> userDishHelper.validateUserDishRequest(dto(1, 0)))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DISH_ID_INVALID);
    }
}
