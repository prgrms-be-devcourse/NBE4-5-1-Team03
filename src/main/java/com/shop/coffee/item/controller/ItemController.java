package com.shop.coffee.item.controller;

import com.shop.coffee.item.dto.ItemDto;
import com.shop.coffee.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


// 모든 상품 조회 API

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;


    @GetMapping
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems(); // 서비스에서 모든 상품 조회 후 반환
    }
}
