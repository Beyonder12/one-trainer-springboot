package com.tokoped.order_service.controller;

import com.tokoped.order_service.dto.BaseResponse;
import com.tokoped.order_service.entity.Order;
import com.tokoped.order_service.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-service")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping("/orders")
    public BaseResponse<List<Order>> inquiryOrder(){
        return orderService.getListOrder();
    }

    @PostMapping("/orders")
    public BaseResponse<Order> createOrder(@RequestBody Order order){
        return orderService.saveOrder(order);
    }

    @PutMapping("/orders/{id}")
    public BaseResponse<Order> updateOrder(@PathVariable String id, @RequestBody Order order){
        return orderService.updateOrder(order, id);
    }

    @DeleteMapping("/orders/{id}")
    public BaseResponse<?> deleteOrder(@PathVariable String id){
        return orderService.deleteOrder(id);
    }
}
