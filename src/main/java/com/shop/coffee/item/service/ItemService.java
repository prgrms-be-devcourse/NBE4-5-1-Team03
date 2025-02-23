package com.shop.coffee.item.service;

import com.shop.coffee.item.dto.ItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shop.coffee.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;


    // 모든 상품 조회 (생성일 내림차순)

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        return itemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemDto::new) // Item -> ItemDto 변환
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id) {
        return itemRepository.findById(id)
                .map(ItemDto::new) // Item → ItemDto 변환
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NOSINGLEORDER.getMessage()));
    }

    @Transactional(readOnly = true)
    public List<Item> getAllItemEntities() {
        return itemRepository.findAll();
    }

    @Transactional
    public Item getItemByIdEntity(long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NOSINGLEORDER.getMessage()));
    }
}
