package com.tokoped.order_service.repository;

import com.tokoped.order_service.entity.Order;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Getter
public class OrderRepository {
    // temporary database
    List<Order> orders = new ArrayList<>();

    // get data order by @Getter
    // abstract

    // save data order
    public Order saveOrder(Order order){
        // set unique id
        order.setOrderId(UUID.randomUUID().toString());

        orders.add(order);
        return order;
    }

    // edit data order
    public Order updateOrder(Order order, String orderId){
        orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst()
                .ifPresent(o -> {
                    o.setUserId(order.getUserId());
                    o.setProductId(order.getProductId());
                    o.setOrderDate(order.getOrderDate());
                    o.setPaymentType(order.getPaymentType());
                });
        return orders.stream().filter(o -> o.getOrderId().equals(orderId))
                .findFirst().orElse(null);
    }

    // delete data order
    public void deleteOrder(String orderId){
        orders.removeIf(o -> o.getOrderId().equals(orderId));
    }
}
