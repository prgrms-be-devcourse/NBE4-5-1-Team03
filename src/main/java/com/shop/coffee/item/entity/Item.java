package com.shop.coffee.item.entity;

import com.shop.coffee.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(length = 255, nullable = true)
    private String description;

    @Column(length = 255, nullable = true)
    private String imagePath;

}