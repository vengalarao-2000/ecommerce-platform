package com.vengalarao.enterprise_order_platform.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vengalarao.enterprise_order_platform.dto.CreateOrderItemRequest;
import com.vengalarao.enterprise_order_platform.dto.CreateOrderRequest;
import com.vengalarao.enterprise_order_platform.dto.OrderItemResponse;
import com.vengalarao.enterprise_order_platform.dto.OrderResponse;
import com.vengalarao.enterprise_order_platform.entity.Customer;
import com.vengalarao.enterprise_order_platform.entity.Order;
import com.vengalarao.enterprise_order_platform.entity.OrderItem;
import com.vengalarao.enterprise_order_platform.entity.Product;
import com.vengalarao.enterprise_order_platform.exception.CustomerNotFoundException;
import com.vengalarao.enterprise_order_platform.exception.ProductNotFoundException;
import com.vengalarao.enterprise_order_platform.repository.CustomerRepository;
import com.vengalarao.enterprise_order_platform.repository.OrderItemRepository;
import com.vengalarao.enterprise_order_platform.repository.OrderRepository;
import com.vengalarao.enterprise_order_platform.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service 
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        CustomerRepository customerRepository,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional 
    public OrderResponse createOrder(CreateOrderRequest request) {
        //1. Check for existing customer with request id passed.
        Customer customer = customerRepository.findById(request.getCustomerId())
                                                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + request.getCustomerId()));
        
        //2. Check if customer is active or not
        //Since active is of type Boolean there might be chances of this field being null to avoid NullPointerException it is safe to use the below instead of !customer.getActive()
        if(!Boolean.TRUE.equals(customer.getActive())) {
            //Later on will create a custom exception like CustomerInactiveException and return 409 CONFLICT as customer exists but their current state does not allow the requested operation.
            throw new IllegalStateException("Customer with id '" + customer.getId() + "' is inactive.");
        }

        
        Map<Long, Integer> quantitiesByProductId = new HashMap<>();
        //Group by productId, sum quantity. {productId: 5, quantity: 3}, {product: 5, quantity: 2} will merge as one {productId: 5, quantity: 5}
        for(CreateOrderItemRequest itemRequest : request.getItems()) {
            quantitiesByProductId.merge(itemRequest.getProductId(), itemRequest.getQuantity(), Integer::sum);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        //3. Loop thru quantitiesByProductId and check if product exists, if active of product is false, if stock quanity request is <= avbl stock quantity.
        for(Map.Entry<Long, Integer> entry : quantitiesByProductId.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            //Check if product id exist
            Product product = productRepository.findById(productId)
                                                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " doesn't exist"));
            //check if product is active
            if(!Boolean.TRUE.equals(product.getActive())) {
                //False and null triggers this block
                //IllegalStateException as we have data in DB but not in a valid state
                throw new IllegalStateException("Product with id " + product.getId() + " is inactive");
            }

            //check if avbl stock quanity > requested quantity
            if(quantity > product.getStockQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            //Once all validations pass, create the OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());

            //Calculate line total
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(lineTotal);
            orderItems.add(orderItem);

        }

        //Iterate over OrderItems and update the product stock. Doing this outside as if the request has 2 products and 1st product passed stock validation and for the second one if validation fails we would have mutated the stock quantity for product 1 already if product is updated inside the loop. Although @Transactional rolls back the changes it is a good practice to update stock quantity once all products validation is done.
        for(OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            //As Product entities are loaded inside the transaction, hibernate tracks them. Their new stock qunatities can be written during flush/commit through dirty checking.
            product.setStockQuantity(product.getStockQuantity() - orderItem.getQuantity());
        }
 
        //Create the Order entity
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");
        //save the order first, this will generate order_id, since order_items.order_id is NOT NULL we need order_id before saving order item and set it.
        Order savedOrder = orderRepository.save(order);

        //Connect each OrderItem back to its Order.
        //This step is mandatory as OrderItem is the owning side of the relationship
        for(OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }

        //save all OrderItems
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        //Now that we have savedOrder and savedOrderItems, next we need to convert these objects into OrderResponse.


        return mapToResponse(savedOrder, savedOrderItems);
                    
    }

    private OrderResponse mapToResponse(Order savedOrder, List<OrderItem> savedOrderItems) {
        List<OrderItemResponse> itemResponses = savedOrderItems
                                                                .stream()
                                                                .map(orderItem -> {
                                                                    BigDecimal lineTotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                                                                    return new OrderItemResponse(
                                                                        orderItem.getId(),
                                                                        orderItem.getProduct().getId(),
                                                                        orderItem.getProduct().getName(),
                                                                        orderItem.getQuantity(),
                                                                        orderItem.getUnitPrice(),
                                                                        lineTotal
                                                                    );
                                                                }).toList();
                                                    
        return new OrderResponse(
            savedOrder.getId(),
            savedOrder.getCustomer().getId(),
            savedOrder.getOrderDate(),
            savedOrder.getTotalAmount(),
            savedOrder.getStatus(),
            itemResponses
        );
    }

}
