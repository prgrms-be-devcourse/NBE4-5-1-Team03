package com.shop.coffee.global.config;

import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {

            //주문이 한개라도 db에 남아있다면 초기화하지 않음
            if (orderRepository.count() > 0)
                return;

            // 상품 생성
            Item item1 = new Item("커피A", "Category1", 100, "Description1", null);
            Item item2 = new Item("커피B", "Category2", 200, "Description2", null);
            Item item3 = new Item("커피C", "Category3", 300, "Description3", null);
            Item item4 = new Item("커피D", "Category4", 400, "Description4", null);

            itemRepository.saveAll(List.of(item1, item2, item3, item4));

            // 주문 생성
            Order order1 = new Order("user1@example.com", "Address1", "Zipcode1", OrderStatus.RECEIVED, 400, null);
            Order order2 = new Order("user2@example.com", "Address2", "Zipcode2", OrderStatus.SHIPPING, 700, null);
            Order order3 = new Order("user3@example.com", "Address3", "Zipcode3", OrderStatus.RECEIVED, 1000, null);
            Order order4 = new Order("user4@example.com", "Address4", "Zipcode4", OrderStatus.SHIPPING, 900, null);
            // 주문 생성 - 동일한 이메일 주문
            Order order5 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.RECEIVED, 100, null);
            Order order6 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.RECEIVED, 500, null); //totalPrice가 하드코딩되어있음
            Order order7 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.SHIPPING, 900, null);
            Order order8 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.SHIPPING, 600, null);
            Order order9 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.SHIPPING, 1000, null);

            // 주문 아이템 생성 및 주문에 추가
            order1.setOrderItems(List.of(new OrderItem(order1, item1, 100, 2, null), new OrderItem(order1, item2, 200, 1, null)));
            order2.setOrderItems(List.of(new OrderItem(order2, item2, 200, 2, null), new OrderItem(order2, item3, 300, 1, null)));
            order3.setOrderItems(List.of(new OrderItem(order3, item3, 300, 2, null), new OrderItem(order3, item4, 400, 1, null)));
            order4.setOrderItems(List.of(new OrderItem(order4, item4, 400, 2, null), new OrderItem(order4, item1, 100, 1, null)));
            // 주문 아이템 생성 및 주문에 추가 - 동일한 이메일 주문
            //1번 1개 RECEIVED 총 100
            order5.setOrderItems(List.of(new OrderItem(order5, item1, 100, 1, null)));
            //2번1개 3번 1개 RECEIVED 총 200+300=500
            order6.setOrderItems(List.of(new OrderItem(order6, item2, 200, 1, null), new OrderItem(order6, item3, 300, 1, null)));
            //1번1개 2번1개 3번 2개 SHIPPING 총 100+200+300*2=900
            order7.setOrderItems(List.of(new OrderItem(order7, item1, 100, 1, null), new OrderItem(order7, item2, 200, 1, null), new OrderItem(order7, item3, 300, 2, null)));
            //3번 2개 SHIPPING 총 300*2=600
            order8.setOrderItems(List.of(new OrderItem(order8, item3, 300, 2, null)));
            //1번 1개 2번 1개 3번 1개 4번 1개 SHIPPING 총 100+200+300+400=1000
            order9.setOrderItems(List.of(new OrderItem(order9, item1, 100, 1, null), new OrderItem(order9, item2, 200, 1, null), new OrderItem(order9, item3, 300, 1, null), new OrderItem(order9, item4, 400, 1, null)));


            orderRepository.saveAll(List.of(order1, order2, order3, order4,order5,order6,order7,order8,order9));
        };
    }
}