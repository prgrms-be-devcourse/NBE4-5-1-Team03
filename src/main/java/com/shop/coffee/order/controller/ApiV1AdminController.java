package com.shop.coffee.order.controller;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.AdminOrderDetailDto;
import com.shop.coffee.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class ApiV1AdminController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public String getOrders(@RequestParam(required = false) OrderStatus orderStatus, Model model) {
        List<com.shop.coffee.order.dto.OrderSummaryDto> orders = orderService.getOrders(orderStatus);
        model.addAttribute("orders", orders);
        return "admin_order_list";
    }

    @GetMapping("/order-detail")
    public String showAdminOrderDetail(@RequestParam("id") Long id, Model model) {
        AdminOrderDetailDto order = orderService.getAdminOrderDetailDtoById(id);
        model.addAttribute("order", order);
        return "admin_order_detail";
    }

    @DeleteMapping("/delete-order")
    @ResponseBody
    public String deleteOrder(@RequestParam("id") Long id) {
        orderService.deleteOrder(id);
        return "admin_order_list";
    }
}

