package com.shop.coffee.order.controller;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.dto.OrderListDto;
import com.shop.coffee.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static com.shop.coffee.global.exception.ErrorCode.NO_EMAIL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApiV1OrderControllerTest4 {

    private MockMvc mockMvc;
    private OrderService orderService; // Mock 객체 생성

    @BeforeEach
    void setUp() {
        orderService = Mockito.mock(OrderService.class); // Mock 객체 명확하게 설정
        ApiV1OrderController orderController = new ApiV1OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    @DisplayName("이메일로 주문 목록 조회 - 정상 동작 및 뷰 반환 테스트")
    void testGetOrderListByEmail() throws Exception {
        // OrderStatus를 명시적으로 설정하여 NPE 방지
        OrderDto mockReceivedOrder = new OrderDto(
                1L, "user5@example.com", "Address5", "Zipcode5",
                OrderStatus.RECEIVED, // OrderStatus 설정
                600, LocalDate.now(), // 수정된 날짜로 변경 필요
                "imagePath",
                2,
                Collections.emptyList() // OrderItem은 비어있는 리스트로 설정
        );

        Map<LocalDate, OrderDto> mockShippingOrdersByDate = Collections.singletonMap(LocalDate.now(), mockReceivedOrder);
        OrderListDto mockOrderListDto = new OrderListDto(mockReceivedOrder, mockShippingOrdersByDate);

        when(orderService.getGroupedOrdersByEmail(anyString())).thenReturn(mockOrderListDto);

        mockMvc.perform(get("/orders/order-list")
                        .param("email", "user5@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_list"))
                .andExpect(model().attributeExists("receivedOrder"))
                .andExpect(model().attributeExists("shippingOrdersByDate"));
    }

//    @Test
//    @DisplayName("존재하지 않는 이메일로 주문 조회 시 예외 발생 테스트")
//    void testGetOrderListByInvalidEmail() throws Exception {
//        // ✅ Mock 객체가 실제 서비스 호출하지 않도록 설정
//        when(orderService.getGroupedOrdersByEmail(anyString()))
//                .thenThrow(new IllegalArgumentException("해당 이메일의 주문은 존재하지 않습니다."));
//
//        mockMvc.perform(get("/orders/order-list")
//                        .param("email", "notfound@example.com"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("error")); // 에러 페이지 뷰가 반환되는지 확인
//    }
}
