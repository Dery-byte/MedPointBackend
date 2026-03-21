package com.medpoint.service;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import java.util.List;

public interface MartService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    ProductResponse restockProduct(Long id, RestockRequest request);
    ProductResponse updateProductPrice(Long id, PriceUpdateRequest request);


    List<String> getCategories();


    TransactionResponse checkout(MartCheckoutRequest request, Long staffId);
}
