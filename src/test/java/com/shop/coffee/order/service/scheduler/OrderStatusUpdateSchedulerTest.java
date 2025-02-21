package com.shop.coffee.order.service.scheduler;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusUpdateSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderStatusUpdateScheduler orderStatusUpdateScheduler;

    private Order receivedOrder;

    @BeforeEach
    void setUp() {
        receivedOrder = Mockito.spy(new Order(
                "test@test.com",
                "서울",
                "222",
                OrderStatus.RECEIVED,
                10000,
                List.of()));
    }

    @Test
    @DisplayName("RECEIVED 상태인 주문이 있는 경우, SHIPPING 상태로 변경되어야 한다.")
    void 주문_상태_업데이트_성공_테스트_1() {
        // given
        when(orderRepository.findByOrderStatus(OrderStatus.RECEIVED))
                .thenReturn(List.of(receivedOrder));

        // when
        orderStatusUpdateScheduler.updateOrderStatus();

        // then
        verify(receivedOrder, times(1)).updateOrderStatus(OrderStatus.SHIPPING);
    }

}