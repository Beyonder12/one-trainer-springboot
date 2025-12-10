package com.tokoped.product_service.controller;

import com.tokoped.product_service.dto.BaseResponse;
import com.tokoped.product_service.entity.Product;
import com.tokoped.product_service.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-service")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public BaseResponse<List<Product>> inquiryProducts(){
        System.out.printf("inquiryProducts()");
        return productService.getProduct();
    }

    @PostMapping("/products")
    public BaseResponse<Product> createProduct(@RequestBody Product product){
        return productService.saveProduct(product);
    }

    @PutMapping("/products/{id}")
    public BaseResponse<Product> updateProduct(@PathVariable String id, @RequestBody Product product){
        return productService.updateProduct(product,id);
    }

    @DeleteMapping("/products/{id}")
    public BaseResponse<?> deleteProduct(@PathVariable String id){
        return productService.deleteProduct(id);
    }

}
