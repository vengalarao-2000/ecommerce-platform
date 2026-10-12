package com.vengalarao.enterprise_order_platform.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.vengalarao.enterprise_order_platform.dto.CreateCustomerRequest;
import com.vengalarao.enterprise_order_platform.dto.CustomerPageResponse;
import com.vengalarao.enterprise_order_platform.dto.CustomerResponse;
import com.vengalarao.enterprise_order_platform.entity.Customer;
import com.vengalarao.enterprise_order_platform.exception.CustomerAlreadyExistsException;
import com.vengalarao.enterprise_order_platform.exception.CustomerNotFoundException;
import com.vengalarao.enterprise_order_platform.repository.CustomerRepository;

@Service 
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if(customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new CustomerAlreadyExistsException("Customer with email '" + request.getEmail() + "' already exists.");
        }
        //Create customer entity, map the request fields to entity, return customer response.
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setActive(true);

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
            () -> new CustomerNotFoundException("Customer with id '" + id + "' not found")
        );

        return mapToResponse(customer);
    }

    public CustomerPageResponse getAllCustomers(int page, int size, String sortBy, String direction) {
        //throw exception if direction is anything other than asc or desc
        if(!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Sort direction must be either 'asc' or 'desc'");
        }
        
        //CHeck the sorting direction first.
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        //Check sortBy word it must be one of the below
        List<String> allowedSortBy = List.of("id", "firstName", "lastName", "email");

        if(!allowedSortBy.contains(sortBy.toLowerCase())) {
            //this prevent arguments like localhost:8080/api/customers?sortBy=salary from reaching Spring Data JPA.
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortDirection, sortBy)
        );

        Page<Customer> customerPage = customerRepository.findAll(pageable);

        //customerPage.getContent() gives List<Customer> tranform it to List<CustomerResponse>
        List<CustomerResponse> responses = customerPage.getContent()
                                                    .stream()
                                                    .map(this::mapToResponse)
                                                    .toList();
        
        return new CustomerPageResponse(
            responses,
            customerPage.getNumber(),
            customerPage.getSize(),
            customerPage.getTotalElements(),
            customerPage.getTotalPages(),
            customerPage.isFirst(),
            customerPage.isLast()
        );
    
        
    }

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setActive(customer.getActive());
        return response;
    }
}
