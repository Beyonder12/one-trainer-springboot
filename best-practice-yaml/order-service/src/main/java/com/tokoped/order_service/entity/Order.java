package com.tokoped.order_service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private String orderId;
    private String productId;
    private String userId;
    private String orderDate;
    private String paymentType;
}
