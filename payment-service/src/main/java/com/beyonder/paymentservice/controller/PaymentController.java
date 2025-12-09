package com.beyonder.paymentservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${environment}")
    private String environment;

    @GetMapping
    public String hello() {
        return "Hello Payments from : " + environment;
    }
}
