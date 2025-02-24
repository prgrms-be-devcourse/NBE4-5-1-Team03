package com.shop.coffee.global.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex) {
        ModelAndView modelAndView = new ModelAndView("error"); // error.html 뷰 사용
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }
}
