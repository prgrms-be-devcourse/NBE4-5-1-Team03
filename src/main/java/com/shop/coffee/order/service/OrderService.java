package com.shop.coffee.order.service;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.OrderIntegrationViewDto;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order create(String email, String address, String zipCode, List<OrderItem> orderItems) {
        Order order = new Order(email, address, zipCode, orderItems);
        return this.orderRepository.save(order);
    }

    @Transactional
    public OrderIntegrationViewDto processPayment(String email, String address, String zipCode, List<OrderItem> orderItems) {
        Optional<Order> orderOptional = this.orderRepository.findByEmailAndOrderStatus(email, OrderStatus.SHIPPING);

        if (orderOptional.isPresent()) {
            Optional<Order> orderWithAddress = this.orderRepository.findByEmailAndOrderStatusAndAddressAndZipcode(
                    email, OrderStatus.SHIPPING, address, zipCode);

            Order newOrder = new Order(email, address, zipCode, orderItems);

            if(orderWithAddress.isPresent()) {
                return new OrderIntegrationViewDto("same_location_order_integration", orderWithAddress.get(), newOrder);
            } else {
                return new OrderIntegrationViewDto("different_location_order_integration", orderOptional.get(), newOrder);
            }
        } else {
            Order newOrder = create(email, address, zipCode, orderItems);
            return new OrderIntegrationViewDto("order_list", null, newOrder);
        }
    }
}
