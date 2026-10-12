package com.vengalarao.enterprise_order_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vengalarao.enterprise_order_platform.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    //THe below method lets service check if (John@gmail.com is present if another user creation request comes with JOHN@gmail.com then this must be checked before insert)
    boolean existsByEmailIgnoreCase(String email);
    
}