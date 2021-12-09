package com.shop.repository;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 상품 레포지토리
 *
 * @author 공통
 * @version 1.0
 */
public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>, ItemRepositoryCustom {

    /**
     * 상품명으로 상품정보을 찾는 쿼리 메소드
     * @param itemNm 상품아이디
     */
    List<Item> findByItemNm(String itemNm);

    /**
     * 상품명이나 상품상세설명으로 상품정보을 찾는 쿼리 메소드
     * @param itemNm 상품아이디
     * @param itemDetail 상품 상세 설명
     */
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);

    /**
     * 가격으로 상품정보을 찾는 쿼리 메소드
     * @param price 상품 가격
     */
    List<Item> findByPriceLessThan(Integer price);

    /**
     * 가격으로 상품정보을 가격 내림차순으로 찾는 쿼리 메소드
     * @param price 상품 가격
     */
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    /**
     * 상품 상세 설명으로 상품정보을 가격 내림차순으로 찾는 쿼리 메소드
     * @param itemDetail 상품 상세 설명
     */
    @Query("select i from Item i where i.itemDetail like %:itemDetail% order by i.price desc")
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);

    /**
     * 상품 상세 설명으로 상품정보을 가격 내림차순으로 찾는 쿼리 메소드(nativeQuery)
     * @param itemDetail 상품 가격
     */
    @Query(value = "select * from item i where i.item_detail like %:itemDetail% order by i.price desc", nativeQuery = true)
    List<Item> findByItemDetailByNative(@Param("itemDetail") String itemDetail);

    /**
     * 검색조건과 페이징 정보, 적용 태그에 따라 상품정보를 찾는 쿼리 메소드(태그 검색페이지)
     *
     * @author Eloy
     * @param filters 적용된 태그들
     * @param itemSearchDto 상품 조회 조건
     * @param pageable 페이징 조건
     */
    Page<MainItemDto> getDetailSearchPage(String[] filters, ItemSearchDto itemSearchDto, Pageable pageable);

}
