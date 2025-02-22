package com.shop.coffee.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    EXAMPLE("예시 메시지입니다."),
    NOSINGLEORDER("해당 주문이 존재하지 않습니다."),
    NOSINGLEITEM("해당 상품이 존재하지 않습니다.");

    private final String message;

}