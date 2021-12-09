package com.shop.repository;

import com.shop.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * 상품 이미지 레포지토리
 *
 * @author 공통
 * @version 1.0
 */
public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {

    /**
     * 상품 이미지 아이디를 오름차순으로 가져오는 쿼리 메소드
     * @param itemId 상품아이디
     */
    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    /**
     * 상품 대표 이미지를 찾는 쿼리 메소드
     * @param itemId 상품아이디
     * @param repImgYn 대표 이미지 여부
     */
    ItemImg findByItemIdAndRepImgYn(Long itemId, String repImgYn);

}
