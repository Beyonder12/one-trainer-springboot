//package com.tokoped.gateway_service.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RouteConfig {
//
//    @Value("${service.product.url}")
//    private String productServiceUrl;
//
//    @Value("${service.user.url}")
//    private String userServiceUrl;
//
//    @Value("${service.order.url}")
//    private String orderServiceUrl;
//
//    @Bean
//    public RouteLocator routes(RouteLocatorBuilder builder){
//        return builder.routes()
//                // product service
//                .route("product-service-root",p -> p
//                        .path("/api/v1/products/**")
//                        .filters(f -> f.rewritePath(
//                                "/api/v1/products","/api/v1/product-service/products"
//                        )) // rewrite path
//                        .uri(productServiceUrl))
//                .route("product-service",p -> p
//                        .path("/api/v1/products/**") // path
//                        .filters(f -> f.rewritePath(
//                                "/api/v1/products/(?<segment>.*)","/api/v1/product-service/products/${segment}"
//                        )) // rewrite path
//                        .uri(productServiceUrl)) // domain
//
//                // user service
//                .route("user-service-root",u -> u
//                        .path("/api/v1/users/**") // path
//                        .filters(f -> f.rewritePath(
//                                "/api/v1/users","/api/v1/user-service/users"
//                        ))
//                        .uri(userServiceUrl)) // domain
//                .route("user-service",u -> u
//                        .path("/api/v1/users/**") // path
//                        .filters(f -> f.rewritePath(
//                                "/api/v1/users/(?<segment>.*)","/api/v1/user-service/users/${segment}"
//                        ))
//                        .uri(userServiceUrl)) // domain
//
//                // order service
//                .route("order-service-root", o -> o
//                        .path("/api/v1/orders/**") // path
//                        .filters(f -> f.rewritePath(
//                                "/api/v1/orders","/api/v1/order-service/orders"
//                        ))
//                        .uri(orderServiceUrl)) //domain
//                .route("order-service", o -> o
//                        .path("/api/v1/orders/**") // path
//                        .filters(f -> f.rewritePath(
//                                "/api/v1/orders/(?<segment>.*)","/api/v1/order-service/orders/${segment}"
//                        ))
//                        .uri(orderServiceUrl)) //domain
//
//                .build();
//    }
//
//}
