package com.shop.coffee.order.repository;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    Optional<Object> findByEmail(String email);
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdOrderWithItems(@Param("id") Long orderId);
}