package com.shop.coffee.order.service.scheduler;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateScheduler {

    private final OrderRepository orderRepository;

    /**
     * 매일 오후 2시마다 RECEIVED 상태인 주문들을 SHIPPING 상태로 갱신
     */
    @Scheduled(cron = "0 0 14 * * ?") // 매일 오후 2시마다 실행
    @Transactional // 트랜잭션 관리
    public void updateOrderStatus() {
        log.info("=== 배송 일괄 처리 시작 ===");

        LocalDateTime start = LocalDateTime.now().minusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now();

        List<Order> receivedOrders = orderRepository.findByCreatedAtBetweenAndOrderStatus(start, end, OrderStatus.RECEIVED);

        receivedOrders.forEach(
                order -> {
                    order.updateOrderStatus(OrderStatus.SHIPPING);
                }
        );

        log.info("=== 배송 일괄 처리 끝 ===");
    }

}