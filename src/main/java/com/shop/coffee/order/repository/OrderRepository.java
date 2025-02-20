package com.shop.coffee.order.repository;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByEmailAndOrderStatus(String email, OrderStatus orderStatus);
    Optional<Order> findByEmailAndOrderStatusAndAddressAndZipcode(String email, OrderStatus orderStatus, String address, String zipCode);
}
