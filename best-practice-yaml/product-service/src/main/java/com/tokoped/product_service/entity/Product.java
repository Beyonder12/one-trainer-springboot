package com.tokoped.product_service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private String productId;
    private String productName;
    private String productPrice;
    private String productStock;
}
