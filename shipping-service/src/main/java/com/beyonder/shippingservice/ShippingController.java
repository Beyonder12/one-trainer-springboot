package com.beyonder.shippingservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shippings")
public class ShippingController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello Shipping Service";
    }
}
