package com.shop.service;

import com.shop.dto.BestItemDto;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 베스트 아이템 서비스
 *
 * @author deneb
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BestItemService {

    private final ItemRepository itemRepository;

    /**
     * 일별 베스트 상품 조회 메소드
     *
     * @return List<BestItemDto> 일별 베스트 아이템 조회결과 목록 반환
     */
    public List<BestItemDto> getBestOfDayItem() {
        return itemRepository.getBestOfDayItem();
    }

    /**
     * 주별 베스트 상품 조회 메소드
     *
     * @return List<BestItemDto> 베스트 아이템 조회결과 목록을 반환
     */
    public List<BestItemDto> getBestOfWeekItem() {
        return itemRepository.getBestOfWeekItem();
    }

    /**
     * 주별 베스트 상품 조회 메소드
     *
     * @return List<BestItemDto> 베스트 아이템 조회결과 목록을 반환
     */
    public List<BestItemDto> getBestOfMonthItem() {
        return itemRepository.getBestOfMonthItem();
    }

}
