package com.example.foodie.ordering.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.common.base.BaseController;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.identity.address.service.AddressService;
import com.example.foodie.ordering.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@RestController
@RequestMapping("${api.prefix}/orders")
@Tag(name = "Order", description = "Đặt món và tra cứu đơn hàng của người dùng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public class OrderController extends BaseController<Order>{
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        super(orderService);
        this.orderService = orderService;
    }

    @Operation(summary = "Lấy danh sách đơn hàng của người dùng", description = "Trả về toàn bộ đơn hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể lấy danh sách đơn hàng")
    })
    @GetMapping("/user")
    public ResponseEntity<List<Order>> getAllOrdersByUserId(Authentication authentication){
        return ResponseEntity.ok(orderService.getAllOrdersByUserId(authentication));
    }

    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Trả về danh sách món ăn trong một đơn hàng cụ thể.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết đơn hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Đơn hàng không tồn tại")
    })
    @GetMapping("/user/{order_id}")
    public ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(
            @Parameter(description = "ID của đơn hàng") @PathVariable(name="order_id") Integer orderId){
        return ResponseEntity.ok(orderService.getAllOrderItems(orderId));
    }

    @Operation(summary = "Tạo đơn hàng", description = "Tạo đơn hàng mới từ giỏ hàng hiện tại của người dùng, giao tới địa chỉ chỉ định.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đơn hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Địa chỉ không hợp lệ hoặc giỏ hàng trống")
    })
    @PostMapping
    public ResponseEntity<Order> createOrder(Authentication authentication,@Valid @RequestBody OrderDTO orderDTO){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(authentication, orderDTO.getAddressId()));
    }
}
