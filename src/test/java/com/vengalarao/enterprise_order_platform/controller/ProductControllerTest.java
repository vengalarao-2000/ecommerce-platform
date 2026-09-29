package com.vengalarao.enterprise_order_platform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.Media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vengalarao.enterprise_order_platform.dto.CreateProductRequest;
import com.vengalarao.enterprise_order_platform.dto.ProductResponse;
import com.vengalarao.enterprise_order_platform.dto.UpdateProductRequest;
import com.vengalarao.enterprise_order_platform.entity.Product;
import com.vengalarao.enterprise_order_platform.exception.ProductAlreadyExistsException;
import com.vengalarao.enterprise_order_platform.exception.ProductNotFoundException;
import com.vengalarao.enterprise_order_platform.service.ProductService;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// For assertions like status().isOk() or content().string(...)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// For utilities like print() to log the request/response
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean 
    private ProductService productService;

    @Test 
    void createProduct_success() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
            "Wireless Mouse",
            "bluetooth mouse",
            new BigDecimal(19.90),
            10
        );

        ProductResponse response = new ProductResponse(
            1L,
            "Wireless Mouse",
            "bluetooth mouse",
            new BigDecimal(19.90),
            10,
            true
        );

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Wireless Mouse"))
        .andExpect(jsonPath("$.price").value(19.90))
        .andExpect(jsonPath("$.stockQuantity").value(10))
        .andExpect(jsonPath("$.active").value(true));

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
        
    }

    //Invalid Create Request must return 400.
    /* For an invalid request like:
    {
        "name": "",
        "description": "Invalid product",
        "price": 0,
        "stockQuantity": -1
    }
    to POST /api/products and content type: application/json
    must return 400 Bad Request because controller has @Valid @RequestBody CreateProductRequest request
    and DTO has constraints like @NotBlank, @Positive, @PositiveOrZero */
    @Test 
    void createProduct_invalidRequest_returnsBadRequest() throws Exception {
        CreateProductRequest request = new CreateProductRequest("", "Invalid request", new BigDecimal(0), -1);
        //no need to set ProductService using when(prodService.createProduct(any(CreateProductRequest.class))).thenReturn(obj);
        //as the service should never be called when invalid data is sent by the user.
        mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

        //check that there is no service layer call.
        verify(productService, never()).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void createProduct_duplicateName_returnsConflict() throws Exception {
        //The main objective of this test:
        /* If ProductService throws ProductAlreadyExistsException then 
        is the controller layer return 409 Conflict? */

        CreateProductRequest request = new CreateProductRequest("Wireless Mouse", "Name already exisits", new BigDecimal(9.99), 10);

        //Set the productService layer to throw an exception when createRequest is called.
        when(productService.createProduct(any(CreateProductRequest.class))).thenAnswer(
            invocation -> {
                CreateProductRequest req = invocation.getArgument(0);
                //Get the name from req and return the exception
                throw new ProductAlreadyExistsException("Product with name '" + req.getName() + "' already exists");
            }
        );

        mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isConflict());

    }

    //Get Product by Id Scenarios: Found (return 200 OK), Not Found (return 404 Not Found)
    @Test
    void getProductById_success() throws Exception {
        ProductResponse response = new ProductResponse(
            1L,
            "Wireless Mouse",
            "Bluetooth mouse",
            new BigDecimal(9.19),
            19,
            true
        );

        when(productService.getProductById(1L)).thenReturn(response);

        //Act and Validate
        mockMvc.perform(
            get("/api/products/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Wireless Mouse"))
        .andExpect(jsonPath("$.description").value("Bluetooth mouse"))
        .andExpect(jsonPath("$.price").value(9.19))
        .andExpect(jsonPath("$.stockQuantity").value(19));
    }

    @Test
    void getProductById_notFound_returnsNotFound() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException("Product with id 999 does not exist."));

        mockMvc.perform(
            get("/api/products/999")
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Product with id 999 does not exist."))
        .andExpect(jsonPath("$.path").value("/api/products/999"));
    }

    //GET /api/products (All Products)
    @Test 
    void getAllProducts_success() throws Exception {
        ProductResponse obj1 = new ProductResponse(
            1L,
            "Wireless Mouse",
            "Bluetooth mouse",
            new BigDecimal(19.99),
            10,
            true
        );
        ProductResponse obj2 = new ProductResponse(
            2L,
            "Wireless Keyboard",
            "Bluetooth keyboard",
            new BigDecimal(29.99),
            20,
            true
        );
        List<ProductResponse> response = List.of(obj1, obj2);
        //Set up when called service method, it'll return response
        when(productService.getAllProducts()).thenReturn(response);

        mockMvc.perform(
            get("/api/products")
        )
        //The JSON response is an array, so $[0] gives you the first object and use .attributeName to get the property that you want.
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].name").value("Wireless Keyboard"))
        .andExpect(jsonPath("$[0].stockQuantity").value("10"));

    }

    @Test 
    void getAllProducts_emptyList() throws Exception {
        //Just return empty list when called service layer getAllProducts.
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(
            get("/api/products")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    }

    //PUT /api/products/id
    //We have 3 different scenarios
    /* 1. Successful Update -> 200 OK
    2. Product Not Found (with specified Id) -> 404 Not Found
    3. Conflict (Duplicate Product Exists with same name) -> 409 CONFLICT */
    void updateProduct_success() throws Exception {
        Long id = 1L;
        UpdateProductRequest request = new UpdateProductRequest(
            "Wireless Mouse",
            "New Description",
            new BigDecimal(89.99),
            4,
            true
        );
        ProductResponse response = new ProductResponse(
            1L,
            "Wireless Mouse",
            "New Description",
            new BigDecimal(89.99),
            4,
            true
        );

        //Set up the service to return response
        //Here the updateProduct is called with value for id as 1L and any object of UpdateProductRequest
        //because When request reached service, the JSON (we passed as request from controller) will be deserialized
        //to a new UpdateProductRequest object, passing actual object (request) to updateProduct() might fail.
/*         In a MockMvc test, JSON is converted by Jackson into a DTO object before the controller calls the service. So the object reaching:
        productService.updateProduct(...)
        may be a new DTO instance, not necessarily the same Java object you created in the test.
        That is why exact-object stubbing can be fragile. */
        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(response);

        //Make controller call using mockMvc
        mockMvc.perform(
            put("/api/products/1")
            //Below two lines resemble the same that we do in Postman, while firing a request
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Wireless Mouse"))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.stockQuantity").value(4));

    }

    @Test 
    void updateProduct_notFound_returnNotFound() throws Exception {
        Long id = 999L;
        UpdateProductRequest request = new UpdateProductRequest(
            "Wireless Mouse",
            "New Description",
            new BigDecimal(18.99),
            17,
            true
        );

        //We don't need ProductResponse, since id does not exist
        when(productService.updateProduct(eq(id), any(UpdateProductRequest.class))).thenThrow(new ProductNotFoundException("Product with id 999 not found"));

        mockMvc.perform(
            put("/api/products/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Product with id 999 not found"))
        .andExpect(jsonPath("$.path").value("/api/products/999"));

    }

    @Test 
    void updateProduct_duplicateName_returnsConflict() throws Exception {
        Long id = 1L;
        UpdateProductRequest request = new UpdateProductRequest(
            "Wireless Mouse",
            "New Desc",
            new BigDecimal(19.99),
            20,
            true
        );

        //Wireless Mouse name is being used by another product with different id. So, exception is thrown.
        when(productService.updateProduct(eq(id), any(UpdateProductRequest.class))).thenAnswer(
            invocation -> {
                UpdateProductRequest obj = invocation.getArgument(1);
                throw new ProductAlreadyExistsException("Product with name " + obj.getName() + " already exists.");
            }
        );

        //Initiate controller call using mockMVC
        mockMvc.perform(
            put("/api/products/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("Product with name Wireless Mouse already exists."))
        .andExpect(jsonPath("$.path").value("/api/products/1"));
    }

    //Delete Scenarios
    //One TC is fine for 1, 2 because soft/hard deletion is taken care by Service layer. 
    /* 1. Active product delete request succeeds
    -> 204 No Content

    2. Inactive product delete request also succeeds
    -> 204 No Content

    3. Product does not exist
    -> 404 Not Found */
    @Test 
    void deleteProduct_success() throws Exception {
        //Since the delete of service doesn't return anything, even the mock does nothing
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(
            delete("/api/products/1")
        )
        .andExpect(status().isNoContent());
        //Check if productService's deleteProduct is called once
        verify(productService, times(1)).deleteProduct(1L);

    }

    @Test 
    void deleteProduct_notFound_returnsNotFound() throws Exception {
        doThrow(
            new ProductNotFoundException("Product with id 999 not found")
        ).when(productService).deleteProduct(999L);

        mockMvc.perform(
            delete("/api/products/999")
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Product with id 999 not found"));

    }

    /* *** IMP MOCKITO RULE ***
    Non-void method:
    when(...).thenReturn(...)
    when(...).thenThrow(...)

    Void method:
    doNothing().when(...)
    doThrow(...).when(...) */

}
