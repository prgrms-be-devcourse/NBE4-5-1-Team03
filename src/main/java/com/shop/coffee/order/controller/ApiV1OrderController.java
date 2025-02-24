package com.shop.coffee.order.controller;

import com.shop.coffee.order.dto.*;
import com.shop.coffee.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.shop.coffee.global.exception.ErrorCode.NO_ORDER_NUMBER;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class ApiV1OrderController {

    private final OrderService orderService;

    // 주문 번호로 주문 단건 조회
    @GetMapping("/{id}")
    public String getOrderById(@PathVariable Long id, Model model) {
        OrderDto orderDto = orderService.getOrderById(id);
        if(orderDto == null) {
            throw new IllegalArgumentException(NO_ORDER_NUMBER.getMessage()+": " + id);
        }
        model.addAttribute("order", orderDto);
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
    @ResponseBody
    public String updateOrder(@PathVariable Long orderId, @ModelAttribute OrderEditDetailDto orderEditDetailDto, RedirectAttributes redirectAttributes) {
        orderService.updateOrder(orderId, orderEditDetailDto);
        // PUT으로 인해. ResponseBody로 수정하였습니다. -> order_detail_modification.js 참조
        return "redirect:/orders/detail/" + orderEditDetailDto.getId(); 
    }

    //입력한 이메일에 따라 마이페이지 또는 메인페이지으로 이동
    @PostMapping("/check-email")
    public String checkEmail(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        boolean emailExists = orderService.emailExists(email);
        if (emailExists) {
            return "redirect:/orders/order-list?email=" + email;
        } else {
            redirectAttributes.addFlashAttribute("message", "해당하는 주문이 없습니다");
            return "redirect:/orders/email-input";
        }
    }

    // 이메일 입력 폼
    @GetMapping("/email-input")
    public String showEmailInputForm() {
        return "email_input";
    }

    // 이메일에 해당하는 주문 목록을 조회 - 주문상태 구분하여 뷰 전달
    @GetMapping("/order-list")
    public String showOrderListGroupByOrderStatus(@RequestParam("email") String email, Model model) {
        OrderListDto orderListDto = orderService.getGroupedOrdersByEmail(email);

        model.addAttribute("receivedOrder", orderListDto.getMergedReceivedOrder());
        model.addAttribute("shippingOrdersByDate", orderListDto.getShippingOrdersByDate());

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
        OrderEditDetailDto orderEditDetailDto = this.orderService.getOrderEditDetailDtoById(id);
        model.addAttribute("orderEditDetail", orderEditDetailDto);
        return "order_detail_modification";
    }
}