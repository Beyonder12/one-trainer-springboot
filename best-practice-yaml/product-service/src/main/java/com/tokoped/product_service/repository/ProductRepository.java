package com.tokoped.product_service.repository;

import com.tokoped.product_service.entity.Product;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Repository
public class ProductRepository {
    // temporary database
    List<Product> products = new ArrayList<>();

    // get data product by @Getter
    // abstract

    // save data product
    public Product saveProducts(Product product){
        // set unique id
        product.setProductId(UUID.randomUUID().toString());

        products.add(product);
        return product;
    }

    // edit data product
    public Product updateProduct(Product product, String productId){
        products.stream().filter(p -> p.getProductId().equals(productId)).findFirst()
                .ifPresent(p -> {
                   p.setProductName(product.getProductName());
                   p.setProductPrice(product.getProductPrice());
                   p.setProductStock(product.getProductStock());
                });
        return products.stream().filter(p -> p.getProductId().equals(productId))
                .findFirst().orElse(null);
    }

    // delete data product
    public void deleteProduct(String productId){
        products.removeIf(p -> p.getProductId().equals(productId));
    }
}
