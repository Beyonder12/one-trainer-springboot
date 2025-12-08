package com.beyonder.productservice.mockapi;

import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public String getProducts() {
        return "Get Products";
    }

    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id, @PathParam("name") String name) {
        long a = 2/id;
        return "Get Product by ID: " + id;
    }

    @PostMapping
    public String createProduct(@RequestBody String body) {
        return "Create Product with data: " + body;
    }

    @PutMapping("/{id}")
    public String updateProduct(@PathVariable Long id, @RequestBody String body) {
        return "Update Product with ID: " + id + ", data: " + body;
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        return "Delete Product with ID: " + id;
    }
}
