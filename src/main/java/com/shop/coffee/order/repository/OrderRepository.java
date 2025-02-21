package com.shop.coffee.order.repository;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 상태에 따라 주문 조회
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    //전체 주문 조회
    List<Order> findAll();

    // 이메일로 주문 유무 확인
    boolean existsByEmail(String email);


    // 주문 생성 시간 내림차순 정렬
    List<Order> findAllByOrderByCreatedAtDesc();
    Optional<Order> findByEmailAndOrderStatus(String email, OrderStatus orderStatus);
    Optional<Order> findByEmailAndOrderStatusAndAddressAndZipcode(String email, OrderStatus orderStatus, String address, String zipCode);

    Optional<Order> findByEmailAndModifiedAt(String email, LocalDateTime modifiedAt);
}