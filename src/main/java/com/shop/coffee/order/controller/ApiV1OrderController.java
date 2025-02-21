package com.shop.coffee.order.controller;

import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class ApiV1OrderController {

    private final OrderService orderService;

    //주문번호(id)로 주문 조회
    @GetMapping("/{id}")
    public String getOrderById(@PathVariable Long id, Model model) {
        OrderDto orderDto = orderService.getOrderById(id);
        model.addAttribute("orders", orderDto);
        return "order_list";
    }

    // 이메일로 주문 조회
    @GetMapping
    public String getOrdersByEmail(@RequestParam(required = false) String email, Model model) {
        if (email != null) {
            List<OrderDto> orders = orderService.getOrdersByEmail(email);
            model.addAttribute("orders", orders);
            return "order_list";
        } else {
            model.addAttribute("orders", List.of()); //데이터가 없을 때 빈 리스트를 전달
            return "same_location_order_integration";
        }
    }
}
