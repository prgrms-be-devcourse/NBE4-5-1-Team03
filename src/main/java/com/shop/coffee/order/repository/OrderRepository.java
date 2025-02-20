package com.shop.coffee.order.repository;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 상태에 따라 주문 조회
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    //전체 주문 조회
    List<Order> findAll();
    // 주문 생성 시간 내림차순 정렬
    List<Order> findAllByOrderByCreatedAtDesc();
}