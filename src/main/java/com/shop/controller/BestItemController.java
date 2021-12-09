package com.shop.controller;

import com.shop.dto.BestItemDto;
import com.shop.service.BestItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 베스트 상품 컨트롤러
 *
 * @author deneb
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/item/best")
@RequiredArgsConstructor
@Tag(name = "베스트 아이템 컨트롤러", description = "베스트 아이템 컨트롤러 목록")
public class BestItemController {

    private final BestItemService bestItemService;

    /**
     * 장바구니 상품 추가 메소드
     *
     * @param model 베스트 상품 목록을 담아 view로 전달하는 객체
     *
     * @return item/bestItem view로 포워딩.
     */
    @GetMapping("/day")
    @Operation(summary = "일별 베스트 상품 조회 메소드", description = "일별 베스트 상품 조회")
    public String getBestItemByDays(Model model) {
        List<BestItemDto> items = bestItemService.getBestOfDayItem();

        model.addAttribute("items", items);

        return "item/bestItem";
    }

    /**
     * 장바구니 상품 추가 메소드
     *
     * @param model 베스트 상품 목록을 담아 view로 전달하는 객체
     *
     * @return item/bestItem view로 포워딩.
     */
    @GetMapping(value = "/week")
    @Operation(summary = "주별 베스트 상품 조회 메소드", description = "일별 베스트 상품 조회")
    public String getBestItemByWeek(Model model){
        List<BestItemDto> items = bestItemService.getBestOfWeekItem();

        model.addAttribute("items", items);

        return "item/bestItem";
    }

    /**
     * 장바구니 상품 추가 메소드
     *
     * @param model 베스트 상품 목록을 담아 view로 전달하는 객체
     *
     * @return item/bestItem view로 포워딩.
     */
    @GetMapping(value = "/month")
    @Operation(summary = "월별 베스트 상품 조회 메소드", description = "일별 베스트 상품 조회")
    public String getBestItemByMonth(Model model){
        List<BestItemDto> items = bestItemService.getBestOfMonthItem();

        model.addAttribute("items", items);

        return "item/bestItem";
    }

}