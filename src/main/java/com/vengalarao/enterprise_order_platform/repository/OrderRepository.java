package com.vengalarao.enterprise_order_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vengalarao.enterprise_order_platform.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
