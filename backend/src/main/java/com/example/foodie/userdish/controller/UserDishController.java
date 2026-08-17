package com.example.foodie.userdish.controller;

import com.example.foodie.userdish.dto.request.UpdateDishQuantityDTO;
import com.example.foodie.userdish.dto.request.UserDishDTO;
import com.example.foodie.userdish.entity.UserDish;
import com.example.foodie.userdish.service.UserDishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@RestController
@RequestMapping("${api.prefix}/user-dishes")
@AllArgsConstructor
@Tag(name = "Cart", description = "Giỏ hàng của người dùng (user-dishes)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public class UserDishController {
    private UserDishService userDishService;

    @Operation(summary = "Lấy giỏ hàng", description = "Trả về toàn bộ món ăn trong giỏ hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy giỏ hàng thành công"),
            @ApiResponse(responseCode = "404", description = "Không thể lấy giỏ hàng")
    })
    @GetMapping
    public ResponseEntity<?> getAllUserDishes(Authentication authentication){
        try{
            List<UserDish> userDishes = userDishService.getAllUserDishesByUserId(authentication);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userDishes);
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Thêm món vào giỏ hàng", description = "Thêm một món ăn với số lượng chỉ định vào giỏ hàng.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm vào giỏ hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Món ăn không tồn tại hoặc dữ liệu không hợp lệ")
    })
    @PostMapping
    public ResponseEntity<?> addUserDish(Authentication authentication, @Valid @RequestBody UserDishDTO userDishDTO){
        try {
            userDishService.addUserDish(authentication, userDishDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build();

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Xoá món khỏi giỏ hàng", description = "Xoá một mục khỏi giỏ hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xoá thành công")
    })
    @DeleteMapping("/{user_dish_id}")
    public ResponseEntity<?> deleteById(
            @Parameter(description = "ID của mục trong giỏ hàng") @PathVariable(name="user_dish_id") Integer userDishId){
        try{
            userDishService.deleteUserDishById(userDishId);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .build();
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Cập nhật số lượng", description = "Cập nhật số lượng của một món trong giỏ hàng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PutMapping
    public ResponseEntity<?> updateQuantity(@Valid @RequestBody UpdateDishQuantityDTO dto){
        try{
            userDishService.updateQuantity(dto.getUserDishId(), dto.getQuantity());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .build();
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
