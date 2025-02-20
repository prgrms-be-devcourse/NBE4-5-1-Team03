package com.shop.coffee.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    EXAMPLE("예시 메시지입니다.");

    private final String message;

}