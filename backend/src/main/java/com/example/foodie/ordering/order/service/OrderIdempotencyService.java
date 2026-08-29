package com.example.foodie.ordering.order.service;

import com.example.foodie.common.idempotency.service.IdempotencyService;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderIdempotencyService {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;
    private final UserHelper userHelper;

    public ResponseEntity<OrderResponseDTO> createOrder(
            Authentication authentication,
            OrderDTO orderDTO,
            String idempotencyKey) {

        Integer userId =
                userHelper.getUserFromAuthentication(authentication).getId();

        return idempotencyService.execute(
                idempotencyKey,
                "CREATE_ORDER",
                userId,
                orderDTO,
                OrderResponseDTO.class,
                () -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(orderService.createOrder(authentication, orderDTO.getAddressId()))
        );
    }
}
