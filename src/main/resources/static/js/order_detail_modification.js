document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.quantity-container').forEach(container => {
        const minusButton = container.querySelector('.minus-button');
        const plusButton = container.querySelector('.plus-button');
        const quantityInput = container.querySelector('.quantity-input');
        const priceElement = container.closest('.order-item').querySelector('.total-price');
        const hiddenTotalPriceInput = container.closest('.order-item').querySelector('input[name^="orderItemEditDetailDtos"][name$=".totalPrice"]');
        const unitPrice = parseInt(container.dataset.unitPrice, 10); // 개당 가격
        const totalPriceElement = document.getElementById('totalPrice'); // 총 결제 금액
        const hiddenTotalPrice = document.getElementById('hiddenTotalPrice'); // 총 결제 금액

        function updatePrice() {
            let quantity = parseInt(quantityInput.value, 10) || 0;
            let totalItemPrice = quantity * unitPrice;

            // 화면 표시 업데이트
            priceElement.innerText = `${totalItemPrice.toLocaleString()}원`;

            // 폼 데이터 반영
            hiddenTotalPriceInput.value = totalItemPrice;

            // 전체 총 결제 금액 업데이트
            updateTotalPrice();
        }

        function updateTotalPrice() {
            let total = 0;
            document.querySelectorAll('input[name^="orderItemEditDetailDtos"][name$=".totalPrice"]').forEach(input => {
                total += parseInt(input.value, 10) || 0;
            });
            totalPriceElement.innerText = `${total.toLocaleString()}원`;
            hiddenTotalPrice.value = total;
        }

        plusButton.addEventListener('click', function () {
            let quantity = parseInt(quantityInput.value, 10) || 0;
            quantity++;
            quantityInput.value = quantity;
            minusButton.disabled = quantity <= 0;
            updatePrice();
        });

        minusButton.addEventListener('click', function () {
            let quantity = parseInt(quantityInput.value, 10) || 0;
            if (quantity > 0) {
                quantity--;
                quantityInput.value = quantity;
                minusButton.disabled = quantity <= 0;
                updatePrice();
            }
        });

        // 수동 입력 처리
        quantityInput.addEventListener('input', function () {
            let value = parseInt(quantityInput.value, 10);
            if (isNaN(value) || value < 0) {
                value = 0;
            }
            quantityInput.value = value;
            minusButton.disabled = value <= 0;
            updatePrice();
        });

        // 엔터 입력 시 폼 제출 방지
        quantityInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
            }
        });

        // 초기 상태 설정
        minusButton.disabled = parseInt(quantityInput.value, 10) <= 0;
        updatePrice(); // 초기값 설정
    });

    // 수정 완료 버튼 클릭 시 PUT 요청 전송
    document.getElementById('submitOrderEdit').addEventListener('click', function () {
        const form = document.getElementById('orderEditForm');
        const orderId = form.dataset.orderId;

        // Form 데이터를 URLSearchParams로 변환
        const formData = new FormData(form);
        const params = new URLSearchParams();
        formData.forEach((value, key) => params.append(key, value));

        fetch(`/orders/${orderId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded" // `@ModelAttribute`와 호환
            },
            body: params.toString()
        }).then(response => {
            if (response.ok) {
                window.location.href = "/orders/detail/" + orderId; // 수정 완료 후 목록 페이지 이동
            } else {
                console.error("주문 수정 실패");
            }
        });
    });

    // 뒤로 가기 버튼 기능 유지
    const backButton = document.getElementById('backButton');
    if (backButton) {
        const id = backButton.dataset.id;
        backButton.addEventListener('click', function () {
            const previousPage = document.referrer;
            if (previousPage.includes('/orders/detail')) {
                history.back();
            } else {
                location.href = `/orders/detail/${id}`;
            }
        });
    }
});
