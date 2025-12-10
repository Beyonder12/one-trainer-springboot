package com.tokoped.product_service.service;

import com.tokoped.product_service.dto.BaseResponse;
import com.tokoped.product_service.entity.Product;
import com.tokoped.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    // dependency injection (DI)
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public BaseResponse<List<Product>> getProduct(){
        Product product=new Product();
        product.setProductId("123");
        product.setProductPrice("123");
        product.setProductName("123");
        product.setProductStock("123");
        return new BaseResponse<>("200","Success get all product",List.of(product));
    }

    public BaseResponse<Product> saveProduct(Product product){
        Product persistProduct = productRepository.saveProducts(product);
        return new BaseResponse<>("200", "Success save product", persistProduct);
    }

    public BaseResponse<Product> updateProduct(Product product, String productId){
        Product updateProduct = productRepository.updateProduct(product, productId);
        return new BaseResponse<>("200", "Success update product", updateProduct);
    }

    public BaseResponse<?> deleteProduct(String productId){
        productRepository.deleteProduct(productId);
        return new BaseResponse<>("200","Success delete product", null);
    }
}
