package com.shop.coffee.order.repository;


import com.shop.coffee.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 주문 생성 시간 내림차순 정렬
    List<Order> findAllByOrderByCreatedAtDesc();
}