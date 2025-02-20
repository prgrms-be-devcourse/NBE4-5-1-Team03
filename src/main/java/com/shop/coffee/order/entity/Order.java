package com.shop.coffee.order.entity;

import com.shop.coffee.global.entity.BaseEntity;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.orderitem.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDERS")
@NoArgsConstructor(access = AccessLevel.PUBLIC) //Protected -> Public 변경. Protected로 하면 테스트 코드에서 에러 발생.
@Getter
@Setter
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String address;

    @Column(length = 30, nullable = false)
    private String zipcode;

    @Column(length = 10, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private int totalPrice;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(String email, String address, String zipcode, List<OrderItem> orderItems) {
        this.email = email;
        this.address = address;
        this.zipcode = zipcode;
        this.orderStatus = OrderStatus.SHIPPING; // 초기 주문 상태
        this.orderItems = orderItems;
        this.totalPrice = calculateTotalPrice(orderItems);
    }

    private int calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

}