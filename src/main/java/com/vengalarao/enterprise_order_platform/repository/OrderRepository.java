package com.vengalarao.enterprise_order_platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vengalarao.enterprise_order_platform.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //Custom JPQL, to fetch orderItems, Product along with order
    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.customer
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        """)
    List<Order> findAllWithDetails();

    //Similar to all orders, even for singlr order fetch we'll write custom JPQL to optimize the # of queries that hibernate writes.
    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.customer
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        WHERE o.id = :id
        """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
