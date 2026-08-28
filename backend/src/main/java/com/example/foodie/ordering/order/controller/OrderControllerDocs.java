package com.example.foodie.ordering.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Order", description = "Đặt món và tra cứu đơn hàng của người dùng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface OrderControllerDocs {

    @Operation(summary = "Lấy danh sách đơn hàng của người dùng", description = "Trả về toàn bộ đơn hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể lấy danh sách đơn hàng")
    })
    ResponseEntity<List<Order>> getAllOrdersByUserId(Authentication authentication);

    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Trả về danh sách món ăn trong một đơn hàng cụ thể.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết đơn hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Đơn hàng không tồn tại")
    })
    ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(
            Authentication authentication,
            @Parameter(description = "ID của đơn hàng") @PathVariable(name="order_id") Integer orderId);

    @Operation(summary = "Tạo đơn hàng", description = "Tạo đơn hàng mới từ giỏ hàng hiện tại của người dùng, giao tới địa chỉ chỉ định.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đơn hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Địa chỉ không hợp lệ hoặc giỏ hàng trống")
    })
    ResponseEntity<Order> createOrder(Authentication authentication, @Valid @RequestBody OrderDTO orderDTO, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey);

    @Operation(summary = "Huỷ đơn hàng của tôi", description = "Cho phép khách hàng huỷ đơn hàng của chính mình, nếu trạng thái hiện tại cho phép huỷ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Huỷ đơn hàng thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ đơn hàng"),
            @ApiResponse(responseCode = "409", description = "Không thể huỷ ở trạng thái hiện tại")
    })
    ResponseEntity<Order> cancelMyOrder(Authentication authentication, @Parameter(description = "ID của đơn hàng") @PathVariable Integer id);

    @Operation(summary = "Xác nhận đã nhận hàng", description = "Cho phép khách hàng xác nhận đã nhận đơn hàng đang giao của chính mình.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ đơn hàng"),
            @ApiResponse(responseCode = "409", description = "Đơn hàng chưa ở trạng thái đang giao")
    })
    ResponseEntity<Order> confirmMyOrderReceived(Authentication authentication, @Parameter(description = "ID của đơn hàng") @PathVariable Integer id);
}
