package com.example.foodie.ordering.userdish.service;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.catalog.image.repository.ImageRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import com.example.foodie.ordering.userdish.entity.UserDish;
import com.example.foodie.ordering.userdish.helper.UserDishHelper;
import com.example.foodie.ordering.userdish.mapper.UserDishMapper;
import com.example.foodie.ordering.userdish.repository.UserDishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Giỏ hàng. Điểm nguy hiểm nhất (item 4): khi cộng dồn số lượng, phải đối chiếu TỔNG
// sau cộng dồn với tồn kho, không phải chỉ phần thêm mới -- nếu không sẽ cho vượt kho
// một cách âm thầm.
@ExtendWith(MockitoExtension.class)
class UserDishServiceImplTest {

    @Mock
    private UserDishRepository userDishRepository;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private Authentication authentication;

    private UserDishServiceImpl userDishService;
    private User caller;

    @BeforeEach
    void setUp() {
        caller = User.builder().id(1).email("caller@test.local").build();
        UserHelper userHelper = new UserHelper(userRepository);
        userDishService = new UserDishServiceImpl(userDishRepository, dishRepository,
                new UserDishHelper(), userHelper, new UserDishMapper(imageRepository));
    }

    private void stubCaller() {
        when(authentication.getName()).thenReturn("caller@test.local");
        when(userRepository.findByEmail("caller@test.local")).thenReturn(Optional.of(caller));
    }

    private static UserDishDTO addDto(Integer dishId, Integer quantity) {
        UserDishDTO dto = new UserDishDTO();
        dto.setDishId(dishId);
        dto.setQuantity(quantity);
        return dto;
    }

    @Nested
    class ThemVaoGio {

        @Test
        @DisplayName("Thêm món không tồn tại ném DISH_NOT_FOUND")
        void should_throwDishNotFound_when_dishDoesNotExist() {
            stubCaller();
            when(dishRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDishService.addUserDish(authentication, addDto(99, 2)))
                    .isInstanceOf(CatalogException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DISH_NOT_FOUND);
        }

        @Test
        @DisplayName("Thêm món chưa có trong giỏ -> upsert đúng số lượng vào DB")
        void should_saveNewCartItem_when_dishNotYetInCart() {
            stubCaller();
            Dish dish = Dish.builder().id(1).stockQuantity(10).build();
            when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
            when(userDishRepository.findByUser_IdAndDish_Id(1, 1)).thenReturn(Optional.empty());

            userDishService.addUserDish(authentication, addDto(1, 2));

            verify(userDishRepository).upsertQuantity(1, 1, 2);
        }

        @Test
        @DisplayName("Giỏ đã có sl 3, thêm tiếp sl 5 -> gửi đúng PHẦN THÊM (5) cho DB cộng dồn, không phải tổng")
        void should_accumulateQuantity_when_dishAlreadyInCart() {
            stubCaller();
            Dish dish = Dish.builder().id(1).stockQuantity(10).build();
            UserDish existing = UserDish.builder().id(50).user(caller).dish(dish).quantity(3).build();
            when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
            when(userDishRepository.findByUser_IdAndDish_Id(1, 1)).thenReturn(Optional.of(existing));

            userDishService.addUserDish(authentication, addDto(1, 5));

            // upsertQuantity nhận delta (5), không phải tổng (8) -- DB tự cộng dồn qua
            // ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity).
            verify(userDishRepository).upsertQuantity(1, 1, 5);
        }

        @Test
        @DisplayName("Giỏ đã có sl 3, thêm tiếp sl 5, kho chỉ 7 -> DISH_OUT_OF_STOCK (đối chiếu tổng sau cộng dồn, không phải phần thêm mới)")
        void should_throwOutOfStock_when_accumulatedTotalExceedsStock() {
            stubCaller();
            Dish dish = Dish.builder().id(1).stockQuantity(7).build();
            UserDish existing = UserDish.builder().id(50).user(caller).dish(dish).quantity(3).build();
            when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
            when(userDishRepository.findByUser_IdAndDish_Id(1, 1)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userDishService.addUserDish(authentication, addDto(1, 5)))
                    .isInstanceOf(CatalogException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DISH_OUT_OF_STOCK);

            verify(userDishRepository, never()).save(any());
        }

        @Test
        @DisplayName("Giỏ đã có sl 3, thêm tiếp sl 5, kho đúng 8 -> thành công (biên qty == stock)")
        void should_succeed_when_accumulatedTotalEqualsStock() {
            stubCaller();
            Dish dish = Dish.builder().id(1).stockQuantity(8).build();
            UserDish existing = UserDish.builder().id(50).user(caller).dish(dish).quantity(3).build();
            when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
            when(userDishRepository.findByUser_IdAndDish_Id(1, 1)).thenReturn(Optional.of(existing));

            userDishService.addUserDish(authentication, addDto(1, 5));

            verify(userDishRepository).upsertQuantity(1, 1, 5);
        }
    }

    @Nested
    class CapNhatSoLuong {

        @Test
        @DisplayName("Cập nhật số lượng về 0 -> mục giỏ bị xoá, không gọi save")
        void should_deleteCartItem_when_updatingQuantityToZero() {
            stubCaller();
            Dish dish = Dish.builder().id(1).stockQuantity(10).build();
            UserDish userDish = UserDish.builder().id(50).user(caller).dish(dish).quantity(3).build();
            when(userDishRepository.findByIdAndUser_Id(50, 1)).thenReturn(Optional.of(userDish));

            userDishService.updateQuantity(authentication, 50, 0);

            verify(userDishRepository).deleteByIdAndUser_Id(50, 1);
            verify(userDishRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cập nhật số lượng vượt kho ném DISH_OUT_OF_STOCK")
        void should_throwOutOfStock_when_updatedQuantityExceedsStock() {
            stubCaller();
            Dish dish = Dish.builder().id(1).stockQuantity(4).build();
            UserDish userDish = UserDish.builder().id(50).user(caller).dish(dish).quantity(3).build();
            when(userDishRepository.findByIdAndUser_Id(50, 1)).thenReturn(Optional.of(userDish));

            assertThatThrownBy(() -> userDishService.updateQuantity(authentication, 50, 5))
                    .isInstanceOf(CatalogException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DISH_OUT_OF_STOCK);
        }

        @Test
        @DisplayName("Cập nhật mục giỏ thuộc người khác ném USERDISH_NOT_FOUND")
        void should_throwUserDishNotFound_when_cartItemBelongsToAnotherUser() {
            stubCaller();
            when(userDishRepository.findByIdAndUser_Id(50, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDishService.updateQuantity(authentication, 50, 2))
                    .isInstanceOf(OrderingException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.USERDISH_NOT_FOUND);
        }
    }
}
