package com.shop.coffee.item.controller;

import com.shop.coffee.item.dto.ItemDto;
import com.shop.coffee.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;


// 모든 상품 조회 API

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;


    @GetMapping
    public String getAllItems(Model model) {
        List<ItemDto> items = itemService.getAllItems();
        model.addAttribute("items", items);
        return "item_list";
    }
    //  상품 단건 조회
    @GetMapping("/{id}")
    public String getItemById(@PathVariable Long id, Model model) {
        ItemDto item = itemService.getItemById(id); // 서비스 호출
        model.addAttribute("item", item); // 뷰에 전달
        return "item_detail"; // item_detail.html 렌더링
    }
}
