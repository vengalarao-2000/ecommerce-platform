package com.vengalarao.enterprise_order_platform.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.processing.Find;
import org.springframework.stereotype.Service;

import com.vengalarao.enterprise_order_platform.dto.CreateProductRequest;
import com.vengalarao.enterprise_order_platform.dto.ProductResponse;
import com.vengalarao.enterprise_order_platform.dto.UpdateProductRequest;
import com.vengalarao.enterprise_order_platform.entity.Product;
import com.vengalarao.enterprise_order_platform.exception.ProductAlreadyExistsException;
import com.vengalarao.enterprise_order_platform.exception.ProductNotFoundException;
import com.vengalarao.enterprise_order_platform.repository.ProductRepository;

@Service
public class ProductService {

    //This is constructor injection. This is preferred because: dependency is explicit, field can be final, service cannot be created without its required repo
    //Field injection will be somthing like: @Autowired on top of productRepository
    private final ProductRepository productRepository;
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        /* 1. Check whether the name already exists, ignoring case.
        2. If it exists, throw an exception.
        3. Create a Product object.
        4. Copy fields from the request.
        5. Set active to true.
        6. Save through ProductRepository.
        7. Return the saved Product. */

        if(productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ProductAlreadyExistsException("Product with name '" + request.getName() + "' already exists");
        }

        //Create a Product object
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        //set server controlled value. Even though entity already sets to 1, to make business decision explicit setting in service as well.
        product.setActive(true);
        //saves to the DB.
        Product savedProduct = productRepository.save(product);

        ProductResponse response = mapToResponse(savedProduct);
        return response;

    }

    //Get product with an id
    public ProductResponse getProductById(Long id) {
        //findById(id) returns Optional<Product> if the method returns Optional[product] then product is returned else (i.e Optional.empty) then exception is thrown by orElseThrow()
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " does not exist."));
        //convert to ProductResponse before returning
        return mapToResponse(product);
    }

    //Get all products
    public List<ProductResponse> getAllProducts() {
        // List<Product> products = productRepository.findAll();
        // List<ProductResponse> response = new ArrayList<>();
        // //Iterate through the products and map them as response objects in response
        // for(Product product : products) {
        //     response.add(mapToResponse(product));
        // }
        // return response;

        //Can write this efficiently using streams
        return productRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
    }


    //PUT/Update a product
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
/*         1. Find product by id
        - if missing -> ProductNotFoundException

        2. Check duplicate name excluding current id
        - if duplicate -> ProductAlreadyExistsException

        3. Update fields on existing Product entity

        4. Save

        5. mapToResponse(savedProduct) */

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product with id "+ id + " not found"));

        if(productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new ProductAlreadyExistsException("Product with name " + request.getName() + " already exists.");
        }

        //3. Update the product fields
        //Update the existing product and save it. Instead of creating a brand new product to prevent overriding of data i.e in an event where 
        //the new product doesn't have some fields, these may override the available values in DB.
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive());
        //Based on the changes we made, hibernate detects the changes made and updates the entity in DB accordingly.
        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);

    }

    public void deleteProduct(Long id) {
        /* Expected Behavior
        i. Product doesn't exist throw 404
        ii. product exists and active = true: then set active = false, save, 204 No Content
        iii. product exists and active = false: then delete row, 204 No Content */

        Product product = productRepository.findById(id).orElseThrow(() -> {
            return new ProductNotFoundException("Product with id " + id + " does not exist");
        });

        //Now apply your logic
        if(product.getActive()) {
            //set to false, save product and 204
            product.setActive(false);
            productRepository.save(product);
        }
        else {
            //delete the row, 204
            productRepository.delete(product);
        }
    }

    //Code reusability: Prevents code duplication
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getActive()
        );
    }


}
