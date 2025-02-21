package com.shop.coffee;

import com.shop.coffee.order.controller.ApiV1AdminController;
import com.shop.coffee.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
public class OrderAdminControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ApiV1AdminController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    @DisplayName("주문 목록 페이지가 정상적으로 로드되는지 테스트")
    public void testGetOrders() throws Exception {
        // when
        // orderService.getOrders(null)이 호출되면 빈 리스트를 반환하도록 설정
        when(orderService.getOrders(null)).thenReturn(Collections.emptyList());

        // then
        // /admin/orders로 GET 요청을 보내고, 상태코드는 200이어야 하며, 뷰 이름은 AdminOrders여야 함
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_order_list"));
    }
}