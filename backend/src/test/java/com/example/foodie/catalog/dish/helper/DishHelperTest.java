package com.example.foodie.catalog.dish.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Nhập kho số lượng <= 0 phải bị chặn vì đây là đường duy nhất làm TĂNG tồn kho —
// cho qua giá trị âm là mở một cửa hậu làm giảm kho mà không qua đơn hàng.
class DishHelperTest {

    private final DishHelper dishHelper = new DishHelper();

    @Test
    @DisplayName("validateStockTopUpQuantity(null) ném DISH_STOCK_QUANTITY_REQUIRED")
    void should_throwStockQuantityRequired_when_quantityIsNull() {
        assertThatThrownBy(() -> dishHelper.validateStockTopUpQuantity(null))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DISH_STOCK_QUANTITY_REQUIRED);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    @DisplayName("validateStockTopUpQuantity với 0 hoặc số âm ném DISH_STOCK_QUANTITY_INVALID")
    void should_throwStockQuantityInvalid_when_quantityIsZeroOrNegative(int quantity) {
        assertThatThrownBy(() -> dishHelper.validateStockTopUpQuantity(quantity))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DISH_STOCK_QUANTITY_INVALID);
    }

    @Test
    @DisplayName("validateStockTopUpQuantity(1) không ném — biên dưới hợp lệ")
    void should_notThrow_when_stockQuantityIsOne() {
        assertThatCode(() -> dishHelper.validateStockTopUpQuantity(1)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {2, -1})
    @DisplayName("validateBlockingType với null, 2 hoặc -1 ném DISH_BLOCK_TYPE_INVALID")
    void should_throwBlockTypeInvalid_when_typeIsOutsideZeroOrOne(Integer type) {
        assertThatThrownBy(() -> dishHelper.validateBlockingType(type))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DISH_BLOCK_TYPE_INVALID);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("validateBlockingType với 0 hoặc 1 không ném")
    void should_notThrow_when_blockingTypeIsZeroOrOne(int type) {
        assertThatCode(() -> dishHelper.validateBlockingType(type)).doesNotThrowAnyException();
    }
}
