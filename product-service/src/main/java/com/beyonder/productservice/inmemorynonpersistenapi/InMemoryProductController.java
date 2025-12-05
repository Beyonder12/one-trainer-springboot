package com.beyonder.productservice.inmemorynonpersistenapi;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/inmemory/products") // prefix updated
public class InMemoryProductController {

    // In-memory non-persistent storage
    private final List<Map<String, String>> db = new ArrayList<>();
    private Long sequence = 1L;

    @GetMapping
    public List<Map<String, String>> getProducts() {
        return db;
    }

    @GetMapping("/{id}")
    public Map<String, String> getProductById(@PathVariable String id) {
        return db.stream()
                .filter(p -> p.get("id").equals(id))
                .findFirst()
                .orElse(Map.of("message", "Product not found"));
    }

    @PostMapping
    public Map<String, String> createProduct(@RequestBody Map<String, String> body) {
        body.put("id", sequence.toString());
        sequence++;
        db.add(body);
        return body;
    }

    @PutMapping("/{id}")
    public Map<String, String> updateProduct(@PathVariable String id, @RequestBody Map<String, String> body) {
        Optional<Map<String, String>> existing = db.stream()
                .filter(p -> p.get("id").equals(id))
                .findFirst();

        if (existing.isPresent()) {
            Map<String, String> product = existing.get();
            product.putAll(body);
            product.put("id", id); // ensure id stays
            return product;
        }
        return Map.of("message", "Product not found");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable String id) {
        boolean removed = db.removeIf(p -> p.get("id").equals(id));
        if (removed) {
            return Map.of("message", "Product deleted");
        }
        return Map.of("message", "Product not found");
    }
}
