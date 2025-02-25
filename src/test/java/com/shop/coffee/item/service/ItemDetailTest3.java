package com.shop.coffee.item.service;

import com.shop.coffee.item.dto.ItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("local")
@DataJpaTest
@Import(ItemService.class)
class ItemDetailTest3 {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("상품 단건 조회 - 성공")
    void getItemById_success() {
        // given
        Item savedItem = itemRepository.save(new Item("커피A", "Category1", 100, "Description1", "/images/coffeeA.png"));

        // when
        ItemDto itemDto = itemService.getItemById(savedItem.getId());

        // then
        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getName()).isEqualTo("커피A");
        assertThat(itemDto.getCategory()).isEqualTo("Category1");
    }

    @Test
    @DisplayName("상품 단건 조회 - 존재하지 않는 상품(실패)")
    void getItemById_notFound() {
        // given
        Long invalidItemId = 999L;

        // when & then
        assertThatThrownBy(() -> itemService.getItemById(invalidItemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 주문이 존재하지 않습니다.");
    }
}
