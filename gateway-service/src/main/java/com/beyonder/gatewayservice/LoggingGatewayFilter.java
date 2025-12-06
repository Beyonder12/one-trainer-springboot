package com.beyonder.gatewayservice;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    public LoggingGatewayFilter() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            // === LOG REQUEST ===
            System.out.println(">>> Request: " 
                    + exchange.getRequest().getMethod() 
                    + " " 
                    + exchange.getRequest().getURI());

            return chain.filter(exchange)
                    .then(
                        Mono.fromRunnable(() -> {
                            // === LOG RESPONSE ===
                            System.out.println("<<< Response: " 
                                    + exchange.getResponse().getStatusCode());
                        })
                    );
        };
    }
}
