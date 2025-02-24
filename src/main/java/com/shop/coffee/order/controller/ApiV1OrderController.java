package com.shop.coffee.order.controller;

import com.shop.coffee.order.dto.OrderDetailDto;
import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.dto.OrderIntegrationDto;
import com.shop.coffee.order.dto.OrderIntegrationRequestDto;
import com.shop.coffee.order.dto.OrderIntegrationViewDto;
import com.shop.coffee.order.dto.OrderPaymentRequestDto;
import com.shop.coffee.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
                request.getEmail(), request.getAddress(), request.getZipCode(), request.getItems()
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

    /**
     * 주문 통합
     * @param requestData 주문 통합 요청 데이터
     * @return 통합 완료 후 리다이렉트할 경로
     */
    @PostMapping("/integrate")
    @ResponseBody
    public ResponseEntity<String> integrateOrders(
            @RequestBody OrderIntegrationRequestDto requestData
    ) {
        OrderIntegrationDto oldOrderDto = requestData.getOldOrder();
        OrderIntegrationDto newOrderDto = requestData.getNewOrder();
        String selectedLocation = requestData.getSelectedLocation();

        orderService.integrateOrders(oldOrderDto.getId(), newOrderDto, selectedLocation);

        String redirectUrl = "/orders/order-list?email=" + oldOrderDto.getEmail();
        return ResponseEntity.ok(redirectUrl);
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

    //입력한 이메일에 따라 마이페이지 또는 메인페이지으로 이동
    @PostMapping("/check-email")
    public String checkEmail(@RequestParam("email") String email) {
        boolean emailExists = orderService.emailExists(email);
        if (emailExists) {
            return ("redirect:/orders/order-list?email=" + email);
        } else {
            return ("redirect:/orders/item-list");
        }
    }

    // 이메일 입력 폼
    @GetMapping("/email-input")
    public String showEmailInputForm() {
        return "email_input";
    }

    // 이메일에 대한 주문 목록 조회 뷰
    @GetMapping("/order-list")
    public String showOrderList(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "order_list";
    }

    // 상품 목록 조회 뷰
    @GetMapping("/item-list")
    public String showItemList() {
        return "item_list";
    }

    @GetMapping("/detail/{id}")
    public String showOrderDetail(@PathVariable long id, Model model) {
        OrderDetailDto orderDetailDto = this.orderService.getOrderDetailDtoById(id);
        model.addAttribute("orderDetail", orderDetailDto);
        return "order_detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        OrderDetailDto orderDetailDto = this.orderService.getOrderDetailDtoById(id);
        model.addAttribute("orderDetail", orderDetailDto);
        return "order_detail_modification";
    }
}