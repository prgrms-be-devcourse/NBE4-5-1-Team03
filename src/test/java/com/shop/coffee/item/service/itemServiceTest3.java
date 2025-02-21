package com.shop.coffee.item.service;

import com.shop.coffee.item.dto.ItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class itemServiceTest3 {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("모든 상품을 생성일 내림차순으로 조회한다.")
    void getAllItems() {
        // 테스트용 Item 객체 두 개 생성
        Item item1 = new Item();
        setField(item1, "name", "원두1");
        setField(item1, "category", "커피");
        setField(item1, "price", 3000);
        setField(item1, "description", "진한 커피");
        setField(item1, "id", 1L);
        setField(item1, "createdAt", LocalDateTime.now().minusDays(1));

        Item item2 = new Item();
        setField(item2, "name", "원두2");
        setField(item2, "category", "커피");
        setField(item2, "price", 4000);
        setField(item2, "description", "부드러운 커피");
        setField(item2, "id", 2L);
        setField(item2, "createdAt", LocalDateTime.now());

        // findAllByOrderByCreatedAtDesc()가 호출되면 item2, item1 순으로 반환하도록 Mock 설정
        when(itemRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(item2, item1));


        List<ItemDto> result = itemService.getAllItems();

        // 결과 검증
        // 1) 반환된 리스트 크기가 2인지
        assertThat(result).hasSize(2);
        // 2) 생성일이 최신인 item2가 리스트의 첫 번째
        assertThat(result.get(0).getName()).isEqualTo("원두2");
        // 3) 그 다음 item1이 두 번째
        assertThat(result.get(1).getName()).isEqualTo("원두1");
    }


    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field;
            try {
                field = target.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
