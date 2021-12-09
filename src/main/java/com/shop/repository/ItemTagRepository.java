package com.shop.repository;

import com.shop.entity.ItemTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 상품 태그 레포지토리
 *
 * @author Eloy
 * @version 1.0
 */
public interface ItemTagRepository extends JpaRepository<ItemTag,Long> {

    /**
     * 상품 아이디로 상품태그를 가져오는 쿼리 메소드
     * @param itemId 상품아이디
     */
    List<ItemTag> findByItemId(@Param(value = "itemId") Long itemId);

}
