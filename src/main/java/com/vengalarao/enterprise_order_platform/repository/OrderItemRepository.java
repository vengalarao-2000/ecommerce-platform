package com.vengalarao.enterprise_order_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vengalarao.enterprise_order_platform.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
