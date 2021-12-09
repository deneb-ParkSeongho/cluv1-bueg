package com.shop.repository;

import com.shop.entity.ReviewImg;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 리뷰 이미지 레포지토리
 * @author 강은별
 * @version 1.0
 */

@Tag(name = "리뷰 이미지 레포지토리", description = "리뷰 이미지 레포지토리 목록")
public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {

    /**
     *
     * @param orderItemId 주문번호가 담겨있는 파라미터
     * @return 상세정보에 보여질 리뷰 이미지 리스트를 반환
     */
    List<ReviewImg> findByOrderItemIdOrderByIdAsc(Long orderItemId);

    /**
     *
     * @param orderItemId 주문번호가 담겨있는 파라미터
     * @return 리뷰 작성할 때 넣은 리뷰 이미지 객체 반환
     */
    ReviewImg findByOrderItemId(Long orderItemId);

}
