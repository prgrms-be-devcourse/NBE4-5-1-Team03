package com.shop.coffee.order.controller;

import com.shop.coffee.order.dto.OrderDetailDto;
import com.shop.coffee.order.dto.OrderDetailDto;
import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.dto.OrderIntegrationViewDto;
import com.shop.coffee.order.dto.OrderPaymentRequestDto;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.service.OrderService;
import com.shop.coffee.orderitem.entity.OrderItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class ApiV1OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public OrderDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/processPayment")
    public String processPayment(Model model, @RequestBody @Valid OrderPaymentRequestDto request
            , RedirectAttributes redirectAttributes) {
        OrderIntegrationViewDto result = orderService.processPayment(
                request.getEmail(), request.getAddress(), request.getZipCode(), request.getOrderItems()
        );

        if (result.getViewName().equals("redirect:/orders/order-list")) {
            redirectAttributes.addAttribute("email", result.getNewOrder().getEmail());
        }

        if (result.getOldOrder() != null) {
            model.addAttribute("oldOrder", result.getOldOrder());
        }
        if (result.getNewOrder() != null) {
            model.addAttribute("newOrder", result.getNewOrder());
        }

        return result.getViewName();
    }

    // 임시 뷰입니다.
    @GetMapping("same_location_order_integration")
    public String same_location_order_integration(Model model) {
        return "";
    }

    // 임시 뷰입니다.
    @GetMapping("different_location_order_integration")
    public String different_location_order_integration(Model model) {
        return "";
    }

    @DeleteMapping("/{orderId}")
    public String deleteOrder(@PathVariable Long orderId, @RequestParam String email, RedirectAttributes redirectAttributes) {
        orderService.deleteOrder(orderId);
        redirectAttributes.addAttribute("email", email);
        return "redirect:/orders";
    }

    @PutMapping("/{orderId}")
    public String updateOrder(@PathVariable Long orderId, @ModelAttribute OrderDetailDto orderDetailDto, RedirectAttributes redirectAttributes) {
        orderService.updateOrder(orderId, orderDetailDto);
        redirectAttributes.addAttribute("email", orderDetailDto.getEmail());
        return "redirect:/orders";
    }
}