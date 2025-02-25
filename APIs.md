# API 명세서

## 1. 루트 리다이렉션

### GET /
- 메인 페이지를 렌더링하는 `/items`로 리다이렉트

---

## 2. 관리자용 주문 내역 조회

### GET /admin/orders
- `@RequestParam(required = false) OrderStatus orderStatus, Model model`
- OrderStatus에 맞는 `OrderSummaryDto`를 가져와 Model에 추가
- OrderStatus가 null이라면 전체 주문내역 조회
- 반환: `admin_order_list`

---

## 3. 관리자용 주문 삭제

### DELETE /admin/delete-order
- `@RequestParam("id") Long id`
- id에 해당하는 주문 삭제
- 반환: `admin_order_list`

---

## 4. 관리자용 주문 상세 조회

### GET /admin/orders/detail/{id}
- `@PathVariable long id, Model model`
- id에 맞는 `OrderDetailDto`를 가져와 Model에 추가
- 반환: `admin_order_detail`

---

## 5. 주문 번호로 주문 단건 조회

### GET /orders/{id}
- `@PathVariable Long id, Model model`
- id에 맞는 주문 단건 조회해 Model에 추가
- id가 null이라면 “해당 주문번호는 존재하지 않습니다.” 예외 반환
- 반환: `order_list`

---

## 6. 주문 결제를 처리하는 엔드포인트

### POST /orders/processPayment
- `Model model, @RequestBody @Valid OrderPaymentRequestDto request, RedirectAttributes redirectAttributes`
- 고객 이메일, 주소, 우편번호, 주문 상품 목록을 받아 결제 처리
- 기존 주문이 있는 경우 OrderIntegrationDto인 oldOrder, newOrder Model 추가
  - 같은 주소와 우편 번호인 경우에 해당하는 뷰 반환 result.getViewName() == same_location_order_integration
  - 다른 주소나 우편 번호인 경우에 해당하는 뷰 반환 result.getViewName() == different_location_order_integration
- 신규 주문일 경우 주문 생성 후 주문 목록 페이지 반환 result.getViewName() == redirect:/orders/order-list?email=" + email
- 반환: `result.getViewName()`

---

## 7. 주문 통합

### POST /orders/integrate
- `@RequestBody OrderIntegrationRequestDto requestData`
- `OrderIntegrationReqeustDto`를 기반으로 주문 통합을 진행하고, 해당 이메일의 주문 내역으로 리다이렉트
- 반환: `ResponseEntity.ok(redirectUrl)`

---

## 8. 주문번호와 이메일에 맞는 주문을 DB에서 삭제

### DELETE /orders/{orderId}
- `@PathVariable Long orderId, @RequestParam String email, RedirectAttributes redirectAttributes`
- id에 맞는 주문을 삭제
- email을 `redirectAttribute`에 추가
- 이메일에 해당하는 마이페이지로 이동
- 반환: `redirect:/orders`

---

## 9. 입력한 이메일에 따라 마이페이지 또는 메인페이지으로 이동

### PUT /orders/{orderId}
- 이메일 존재 시, 즉시 반환1
- 이메일 미존재 시: message를 `redirectAttribute`에 추가하여 반환 2
- 반환1: `"redirect:/orders/order-list?email=" + email`
- 반환2: `redirect:/orders/email-input`

---

## 10. 입력한 이메일 저장소 존재 여부 확인

### POST /orders/check-email
- `@RequestParam("email") String email, RedirectAttributes redirectAttributes`
- 이메일 저장소 존재 여부에 따라 두 가지 URL로 리다이렉트
- 해당 이메일이 존재한다면 해당 이메일에 대한 주문내역조회
- 해당 이메일이 존재하지 않는다면 메세지와 함께 `/orders/email-input`로 리다이렉트
- `email_input.html`에 `redirectAttributes`이 전달되므로 다시 메인페이지인 `/items`로 리다이렉트
- 반환: 해당 이메일이 존재한다면 `"redirect:/orders/order-list?email=" + email`
  존재하지 않는다면 `"redirect:/orders/email-input"`

---

## 11. 이메일 입력 페이지

### GET /orders/email-input
- 이메일을 입력할 수 있는 뷰 반환
- 반환: `email_input`

---

## 12. 이메일에 해당하는 주문 조회

### GET /orders/order-list
- `@RequestParam("email") String email, Model model`
- 이메일에 해당하는 주문 상태 (RECEIVED, SHIPPING)을 각각 2개의 Model에 추가
- 반환: `order_list`

---

## 13. 주문번호에 해당하는 주문 상세 조회 뷰로 이동

### GET /orders/detail/{id}
- `@PathVariable long id, Model model`
- id에 맞는 `orderDetailDto`를 Model에 추가
- 반환: `order_detail`

---

## 14. 주문번호에 해당하는 주문 수정 뷰로 이동

### GET /orders/edit/{id}
- `@PathVariable long id, Model model`
- id에 맞는 `orderEditDetailDto`를 Model에 추가
- 반환: `order_detail_modification`

---

## 15. 모든 아이템 조회 뷰로 이동

### GET /items
- 모든 item을 담은 `ItemDto`를 Model에 추가
- 반환: `item_list`

---

## 16. 아이템 단건 조회

### GET /items/{id}
- `@PathVariable Long id, Model model`
- id에 맞는 `itemDto`를 Model에 추가
- 반환 뷰: `item_detail`
