package com.example.foodie.identity.admin.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderStatusUpdateDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - Order", description = "Quản trị đơn hàng (chỉ ADMIN)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminOrderControllerDocs {

    ResponseEntity<List<OrderResponseDTO>> getAll();

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công"),
            @ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại")
    })
    ResponseEntity<OrderResponseDTO> getById(@PathVariable Integer id);

    ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(@PathVariable Integer id);

    @Operation(summary = "Xoá đơn hàng", description = "Đơn hàng đã DELIVERED được giữ lại cho audit/reporting và không thể xoá bằng thao tác này.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xoá thành công"),
            @ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại"),
            @ApiResponse(responseCode = "409", description = "Đơn hàng đã DELIVERED, không thể xoá")
    })
    ResponseEntity<Void> deleteById(@PathVariable Integer id);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại"),
            @ApiResponse(responseCode = "409", description = "Không thể chuyển sang trạng thái này")
    })
    ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Integer id, @Valid @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO);
}
