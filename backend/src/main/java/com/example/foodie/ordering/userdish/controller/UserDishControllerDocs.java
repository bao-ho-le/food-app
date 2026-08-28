package com.example.foodie.ordering.userdish.controller;

import com.example.foodie.ordering.userdish.dto.request.UpdateDishQuantityDTO;
import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import com.example.foodie.ordering.userdish.dto.response.UserDishResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Cart", description = "Giỏ hàng của người dùng (user-dishes)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface UserDishControllerDocs {

    @Operation(summary = "Lấy giỏ hàng", description = "Trả về toàn bộ món ăn trong giỏ hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy giỏ hàng thành công"),
            @ApiResponse(responseCode = "404", description = "Không thể lấy giỏ hàng")
    })
    ResponseEntity<List<UserDishResponseDTO>> getAllUserDishes(Authentication authentication);

    @Operation(summary = "Thêm món vào giỏ hàng", description = "Thêm một món ăn với số lượng chỉ định vào giỏ hàng.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm vào giỏ hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Món ăn không tồn tại hoặc dữ liệu không hợp lệ")
    })
    ResponseEntity<Void> addUserDish(Authentication authentication, @Valid @RequestBody UserDishDTO userDishDTO);

    @Operation(summary = "Xoá món khỏi giỏ hàng", description = "Xoá một mục khỏi giỏ hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xoá thành công")
    })
    ResponseEntity<Void> deleteById(
            Authentication authentication,
            @Parameter(description = "ID của mục trong giỏ hàng") @PathVariable(name="user_dish_id") Integer userDishId);

    @Operation(summary = "Cập nhật số lượng", description = "Cập nhật số lượng của một món trong giỏ hàng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    ResponseEntity<Void> updateQuantity(Authentication authentication, @Valid @RequestBody UpdateDishQuantityDTO dto);
}
