package com.vengalarao.enterprise_order_platform.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vengalarao.enterprise_order_platform.dto.CreateProductRequest;
import com.vengalarao.enterprise_order_platform.dto.ProductResponse;
import com.vengalarao.enterprise_order_platform.dto.UpdateProductRequest;
import com.vengalarao.enterprise_order_platform.service.ProductService;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/api/products")
public class ProductController {
    //Use constructor injection for ProductService
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        /*
            @PostMapping: Maps POST /api/products to this method.
            @RequestBody: Converts incoming JSON into CreateProductRequest
            @Valid: Runs the jakarta validation annotations in CreateProductRequest. Ex: stockQuanity: -10 will be flagged and fails before service method is called
            ResponseEntity: Allows to control both HTTP status AND response body
        */

        //1. Call service
        ProductResponse response = productService.createProduct(request);
        //2. return HTTP 201 i.e created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        //This produces 204 No Content response once delete logic is executed.
        return ResponseEntity.noContent().build();
    }    

}
