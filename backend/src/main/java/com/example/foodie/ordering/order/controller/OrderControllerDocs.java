package com.example.foodie.ordering.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
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
            @ApiResponse(responseCode = "404", description = "Tài khoản trong token không còn tồn tại")
    })
    ResponseEntity<List<OrderResponseDTO>> getAllOrdersByUserId(Authentication authentication);

    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Trả về danh sách món ăn trong một đơn hàng cụ thể.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết đơn hàng thành công"),
            @ApiResponse(responseCode = "403", description = "Đơn hàng không thuộc về người dùng hiện tại"),
            @ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại")
    })
    ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(
            Authentication authentication,
            @Parameter(description = "ID của đơn hàng") @PathVariable(name="order_id") Integer orderId);

    @Operation(summary = "Tạo đơn hàng", description = "Tạo đơn hàng mới từ toàn bộ giỏ hàng hiện tại, giao tới địa chỉ chỉ định. "
            + "All-or-nothing: nếu bất kỳ món nào trong giỏ không khả dụng hoặc không đủ tồn kho, "
            + "không đơn nào được tạo và giỏ hàng giữ nguyên. "
            + "Header Idempotency-Key (tối đa 36 ký tự, tuỳ chọn) đảm bảo gọi lại với cùng key và cùng "
            + "request body sẽ nhận lại đúng response của lần gọi thành công đầu tiên thay vì tạo đơn mới; "
            + "gửi lại cùng key với body khác bị từ chối.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đơn hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ, giỏ hàng trống, hoặc Idempotency-Key dài quá 36 ký tự"),
            @ApiResponse(responseCode = "404", description = "Địa chỉ không tồn tại hoặc không thuộc về người dùng hiện tại"),
            @ApiResponse(responseCode = "409", description = "Có món trong giỏ không khả dụng/không đủ tồn kho, hoặc request với cùng Idempotency-Key đang được xử lý"),
            @ApiResponse(responseCode = "422", description = "Idempotency-Key đã được dùng trước đó với request body khác")
    })
    ResponseEntity<OrderResponseDTO> createOrder(Authentication authentication, @Valid @RequestBody OrderDTO orderDTO, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey);

    @Operation(summary = "Huỷ đơn hàng của tôi", description = "Cho phép khách hàng huỷ đơn hàng của chính mình khi đơn còn ở PENDING hoặc PREPARING (trước khi shipper nhận hàng). Tồn kho các món trong đơn được hoàn trả.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Huỷ đơn hàng thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ đơn hàng"),
            @ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại"),
            @ApiResponse(responseCode = "409", description = "Đơn hàng đã DELIVERING/DELIVERED/CANCELLED, không thể huỷ")
    })
    ResponseEntity<OrderResponseDTO> cancelMyOrder(Authentication authentication, @Parameter(description = "ID của đơn hàng") @PathVariable Integer id);

    @Operation(summary = "Xác nhận đã nhận hàng", description = "Cho phép khách hàng xác nhận đã nhận đơn hàng đang giao của chính mình.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ đơn hàng"),
            @ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại"),
            @ApiResponse(responseCode = "409", description = "Đơn hàng chưa ở trạng thái đang giao")
    })
    ResponseEntity<OrderResponseDTO> confirmMyOrderReceived(Authentication authentication, @Parameter(description = "ID của đơn hàng") @PathVariable Integer id);
}
