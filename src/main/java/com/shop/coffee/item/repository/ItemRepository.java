package com.shop.coffee.item.repository;

import com.shop.coffee.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 모든 상품을 생성일 내림차순으로 조회
    List<Item> findAllByOrderByCreatedAtDesc();

    // 카테고리별 상품 조회 (추후 구현 예정)
    List<Item> findByCategory(String category);

}
