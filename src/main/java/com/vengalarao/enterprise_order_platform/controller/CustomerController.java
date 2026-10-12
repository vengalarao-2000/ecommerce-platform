package com.vengalarao.enterprise_order_platform.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vengalarao.enterprise_order_platform.dto.CreateCustomerRequest;
import com.vengalarao.enterprise_order_platform.dto.CustomerPageResponse;
import com.vengalarao.enterprise_order_platform.dto.CustomerResponse;
import com.vengalarao.enterprise_order_platform.repository.CustomerRepository;
import com.vengalarao.enterprise_order_platform.service.CustomerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController 
@RequestMapping("/api/customers")
@Validated 
public class CustomerController {

    //Constructor Injection: to get object of service class
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping()
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping()
    public ResponseEntity<CustomerPageResponse> getAllCustomer(
        @RequestParam(defaultValue = "0") 
        @Min(value = 0, message = "Page number cannot be negative") int page,
        @RequestParam(defaultValue = "5") 
        @Min(value = 1, message = "Page size must be at least 1") 
        @Max(value = 100, message = "Page size cannot exceed 100")
        int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction
    ) {
        CustomerPageResponse response = customerService.getAllCustomers(page, size, sortBy, direction);
        return ResponseEntity.ok(response);
    }
    

    
    
    
}
