package com.shop.coffee.order.controller;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.dto.OrderIntegrationDto;
import com.shop.coffee.order.dto.OrderPaymentRequestDto;
import com.shop.coffee.order.service.OrderService;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1OrderControllerTest5 {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    @DisplayName("결제 처리 시 주문이 없을 경우 orders?email 반환")
    void processPaymentTest_whenOrderNotExists() throws Exception {
        // DTO 객체 생성
        OrderPaymentRequestDto orderPaymentRequestDto = createOrderPaymentRequestDto(
                "test@example.com", "서울특별시 5A", "12345", 1);

        performProcessPaymentRequest(orderPaymentRequestDto)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/order-list?email=test%40example.com")); // 반환된 인코딩된 문자열 확인
    }

    @Test
    @DisplayName("결제 처리 시 주문이 존재하고, 주소와 우편번호가 같을 경우 same_location_order_integration 뷰 반환 및 newOrder 모델 검증")
    void processPaymentTest_whenOrderExistsAndAddressEquals() throws Exception {
        createOrder("test@example.com", "서울특별시 5A", "12345", List.of(1L, 2L));

        OrderPaymentRequestDto orderPaymentRequestDto = createOrderPaymentRequestDto(
                "test@example.com", "서울특별시 5A", "12345", 1);

        // 요청 수행 및 결과 추출
        MvcResult mvcResult = performProcessPaymentRequest(orderPaymentRequestDto)
                .andExpect(view().name("same_location_order_integration"))
                .andReturn();

        modelAndViewEmailTest(mvcResult, "test@example.com");
    }

    @Test
    @DisplayName("결제 처리 시 주문이 존재하고, 주소와 우편번호가 다를 경우 order_integration 뷰 반환 및 newOrder 모델 검증")
    void processPaymentTest_whenOrderExistsAndAddressNotEquals() throws Exception {

        createOrder("test@example.com", "서울특별시 1A", "12345", List.of(1L, 2L));

        OrderPaymentRequestDto orderPaymentRequestDto = createOrderPaymentRequestDto(
                "test@example.com", "서울특별시 5A", "12345", 1);

        // 요청 수행 및 결과 추출
        MvcResult mvcResult = performProcessPaymentRequest(orderPaymentRequestDto)
                .andExpect(view().name("different_location_order_integration"))
                .andReturn();

        modelAndViewEmailTest(mvcResult, "test@example.com");
    }

    private OrderPaymentRequestDto createOrderPaymentRequestDto(String email, String address, String zipCode, long itemId) {
        List<ItemToOrderItemDto> items = new ArrayList<>();
        items.add(new ItemToOrderItemDto(itemRepository.getById(itemId), 1));
        return new OrderPaymentRequestDto(email, address, zipCode, items);
    }

    private void createOrder(String email, String address, String zipCode, List<Long> itemIds) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (Long itemId : itemIds) {
            orderItems.add(orderItemRepository.findById(itemId).get());
        }
        orderService.create(email, address, zipCode, orderItems);
    }

    private ResultActions performProcessPaymentRequest(OrderPaymentRequestDto requestDto) throws Exception {
        return mvc.perform(post("/orders/processPayment")
                        .flashAttr("orderPaymentRequestDto", requestDto)
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andDo(print());
    }


    private void modelAndViewEmailTest(MvcResult mvcResult, String email) {
        // ModelAndView 추출 및 검증
        ModelAndView mav = mvcResult.getModelAndView();
        assertThat(mav).isNotNull();

        // newOrder 모델 속성이 있는지 확인
        Object newOrderObj = mav.getModel().get("newOrder");
        assertThat(newOrderObj).isNotNull();

        // Order 객체로 캐스팅 후, email 속성 검증
        OrderIntegrationDto newOrder = (OrderIntegrationDto) newOrderObj;
        assertThat(newOrder.getEmail()).isEqualTo(email);
    }
}