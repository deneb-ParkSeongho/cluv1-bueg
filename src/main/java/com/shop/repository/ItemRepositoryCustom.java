package com.shop.repository;

import com.shop.dto.*;
import com.shop.entity.Item;
import org.iq80.snappy.Main;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
/**
 * 상품 사용자 정의 레포지토리
 *
 * @author 공통
 * @version 1.0
 */
public interface ItemRepositoryCustom {

    /**
     * 검색조건과 페이징 정보에 따라 상품정보를 찾는 쿼리 메소드(상품관리페이지)
     * @param itemSearchDto 상품 조회 조건
     * @param pageable 페이징 정보
     */
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    /**
     * 검색조건과 페이징 정보에 따라 상품정보를 찾는 쿼리 메소드(메인페이지)
     * @param itemSearchDto 상품 조회 조건
     * @param pageable 페이징 정보
     */
    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<GiftMainItemDto> getGiftItemPage(ItemSearchDto itemSearchDto, Pageable pageable, Long cateCode);

    /**
     * 일별 베스트 상품 조회 메소드
     *
     * @return List<BestItemDto> 조회된 일별 베스트 상품 리스트 반환
     */
    List<BestItemDto> getBestOfDayItem();

    /**
     * 주별 베스트 상품 조회 메소드
     *
     * @return List<BestItemDto> 조회된 주별 베스트 상품 리스트 반환
     */
    List<BestItemDto> getBestOfWeekItem();

    /**
     * 월별 베스트 상품 조회 메소드
     *
     * @return List<BestItemDto> 조회된 월별 베스트 상품 리스트 반환
     */
    List<BestItemDto> getBestOfMonthItem();

    Page<MainItemDto> getDetailSearchPage(String[] filters, ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getComplexSearchPage(ItemComplexSearchDto itemComplexSearchDto, Pageable pageable);

}
