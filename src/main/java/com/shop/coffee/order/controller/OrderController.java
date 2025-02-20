package com.shop.coffee.order.controller;

import com.shop.coffee.order.DTO.OrderSummaryDTO;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/orders")
    public String getOrders(@RequestParam(required = false) OrderStatus orderStatus, Model model) {
        List<OrderSummaryDTO> orders = orderService.getOrders(orderStatus);
        model.addAttribute("orders", orders);
        return "admin_order_list";
    }
}