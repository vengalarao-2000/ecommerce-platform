package com.vengalarao.enterprise_order_platform.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.vengalarao.enterprise_order_platform.dto.CreateProductRequest;
import com.vengalarao.enterprise_order_platform.dto.ProductResponse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest 
@AutoConfigureMockMvc 
@ActiveProfiles ("test")
class ProductIntegrationTest {

    @Autowired 
    private MockMvc mockMvc;

    @Autowired 
    private ObjectMapper objectMapper;

    @Test 
    void createProduct_integration_success() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
            "Integration Mouse A",
            "Integration Test Description",
            new BigDecimal(9.99),
            20
        );

        mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Integration Mouse A"))
        .andExpect(jsonPath("$.price").value(9.99))
        .andExpect(jsonPath("$.stockQuantity").value(20))
        .andExpect(jsonPath("$.active").value(true)); 
    }

    @Test
/*     It is always a good practice to create the test data you want for the scenario instead of assuming it
    is already present (Also don't think that data comes from previous tests). Here we have created a
    product using POST then captured the response, converted into ProductResponse using ObjectMapper
     then made a GET request */
    void createThenGetProduct_integration_success() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
            "Integration Keyboard",
            "Integration description",
            new BigDecimal(19.99),
            10
        );

        String createResponse = mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        //Below 3 lines Capture the JSON response returned by POST method.
        .andReturn()
        .getResponse()
        .getContentAsString();

        //Convert the response back to ProductResponse using objectMapper.
        ProductResponse response = objectMapper.readValue(createResponse, ProductResponse.class);

        //Get the productId using getter then make a GET request for product with that Id.
        Long productId = response.getId();

        mockMvc.perform(
            get("/api/products/" + productId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(productId))
        .andExpect(jsonPath("$.name").value("Integration Keyboard"))
        .andExpect(jsonPath("$.description").value("Integration description"))
        .andExpect(jsonPath("$.price").value(19.99))
        .andExpect(jsonPath("$.stockQuantity").value(10))
        .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    /* Product exists by name already, then an exception is thrown with 409 conflict  */
    void createProduct_duplicateName_returnsConflict() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
            "Integration Mouse",
            "Integration test descritpion",
            new BigDecimal(99.99),
            2
        );

        //add this product by using POST method
        mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated());

        //Make another POST request with same product (to simulate duplicate scenario)
        mockMvc.perform(
            post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    //Missing product -> returns 404 Not Found
    void getProductById_notFound_returnsNotFound() throws Exception {
        mockMvc.perform(
            get("/api/products/999")
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"));
    }

    

}