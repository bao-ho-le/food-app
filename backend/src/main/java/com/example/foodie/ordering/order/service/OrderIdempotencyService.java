package com.example.foodie.ordering.order.service;

import com.example.foodie.common.idempotency.service.IdempotencyService;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.ordering.order.entity.Order;
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

    public ResponseEntity<Order> createOrder(
            Authentication authentication,
            Integer addressId,
            String idempotencyKey) {

        Integer userId =
                userHelper.getUserFromAuthentication(authentication).getId();

        return idempotencyService.execute(
                idempotencyKey,
                "CREATE_ORDER",
                userId,
                Order.class,
                () -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(orderService.createOrder(authentication, addressId))
        );
    }
}
