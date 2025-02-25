package com.shop.coffee.order.controller;

import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.dto.OrderIntegrationDto;
import com.shop.coffee.order.service.OrderService;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
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
        String jsonPayload = createOrderJson("test@example.com", "서울특별시 5A",
                "12345", 1, "coffee 1", 1000, 2, "path/to/image.jpg");

        mvc.perform(post("/orders/processPayment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(jsonPayload))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/order-list?email=test%40example.com")); // 반환된 인코딩된 문자열 확인
    }

    @Test
    @DisplayName("결제 처리 시 주문이 존재하고, 주소와 우편번호가 같을 경우 same_location_order_integration 뷰 반환 및 newOrder 모델 검증")
    void processPaymentTest_whenOrderExistsAndAddressEquals() throws Exception {
        OrderItem orderItem1 = orderItemRepository.findById(1L).get();
        OrderItem orderItem2 = orderItemRepository.findById(2L).get();
        orderService.create("test@example.com", "서울특별시 5A", "12345", List.of(orderItem1, orderItem2));

        String jsonPayload = createOrderJson("test@example.com", "서울특별시 5A",
                "12345", 1, "coffee 1",1000, 2, "path/to/image.jpg");

        // 요청 수행 및 결과 추출
        MvcResult mvcResult = mvc.perform(post("/orders/processPayment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(jsonPayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("same_location_order_integration"))
                .andReturn();

        modelAndViewEmailTest(mvcResult, "test@example.com");
    }

    @Test
    @DisplayName("결제 처리 시 주문이 존재하고, 주소와 우편번호가 다를 경우 order_integration 뷰 반환 및 newOrder 모델 검증")
    void processPaymentTest_whenOrderExistsAndAddressNotEquals() throws Exception {

        OrderItem orderItem1 = orderItemRepository.findById(1L).get();
        OrderItem orderItem2 = orderItemRepository.findById(2L).get();
        orderService.create("test@example.com", "서울특별시 1A", "12345", List.of(orderItem1, orderItem2));

        String jsonPayload = createOrderJson("test@example.com", "서울특별시 5A",
                "12345", 1, "coffee 1", 1000, 2, "path/to/image.jpg");

        // 요청 수행 및 결과 추출
        MvcResult mvcResult = mvc.perform(post("/orders/processPayment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(jsonPayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("different_location_order_integration"))
                .andReturn();

        modelAndViewEmailTest(mvcResult, "test@example.com");
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

    private String createOrderJson(String email, String address, String zipCode, long id, String name, int price, int quantity, String imagePath) {
        return "{" +
                "\"email\": \"" + email + "\"," +
                "\"address\": \"" + address + "\"," +
                "\"zipCode\": \"" + zipCode + "\"," +
                "\"items\": [" +
                "{" +
                "\"id\": " + id + "," +
                "\"name\": \"" + name + "\"," +
                "\"price\": " + price + "," +
                "\"quantity\": " + quantity + "," + // quantity 추가
                "\"imagePath\": \"" + imagePath + "\"" +
                "}" +
                "]" +
                "}";
    }

}
