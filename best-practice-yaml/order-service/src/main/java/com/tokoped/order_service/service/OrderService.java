package com.tokoped.order_service.service;

import com.tokoped.order_service.dto.BaseResponse;
import com.tokoped.order_service.entity.Order;
import com.tokoped.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    // dependency injection (DI)
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public BaseResponse<List<Order>> getListOrder(){
        return new BaseResponse<>("200","Success get data order", orderRepository.getOrders());
    }

    public BaseResponse<Order> saveOrder(Order order){
        Order persistOrder = orderRepository.saveOrder(order);
        return new BaseResponse<>("200","Success save data order", persistOrder);
    }


    public BaseResponse<Order> updateOrder(Order order, String orderId){
        Order updateOrder = orderRepository.updateOrder(order, orderId);
        return new BaseResponse<>("200","Success update data order", updateOrder);
    }

    public BaseResponse<?> deleteOrder(String orderId){
        orderRepository.deleteOrder(orderId);
        return new BaseResponse<>("200","Success delete data order", null);
    }
}
