package com.shop.controller;

import com.shop.dto.BestItemDto;
import com.shop.service.BestItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * @author 박성호
 * @version 1.0
 */
@Tag(name = "베스트 상품", description = "베스트 상품 관련 요청 처리")
@Slf4j
@Controller
@RequestMapping("/item/best")
@RequiredArgsConstructor
public class BestItemController {

    private final BestItemService bestItemService;

    /**
     * 일간 베스트 상품 페이지
     *
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 일간 베스트 상품 페이지 뷰 경로
     */
    @Operation(summary = "일간 베스트 상품 페이지", description = "일간 베스트 상품 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일간 베스트 상품 페이지 뷰")
    })
    @GetMapping("/day")
    public String getBestItemByDays(Model model) {
        List<BestItemDto> items = bestItemService.getBestOfDayItem();

        model.addAttribute("title", "일간");
        model.addAttribute("items", items);

        return "item/bestItem";
    }

    /**
     * 주간 베스트 상품 페이지
     *
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 주간 베스트 상품 페이지 뷰 경로
     */
    @Operation(summary = "주간 베스트 상품 페이지", description = "주간 베스트 상품 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주간 베스트 상품 페이지 뷰")
    })
    @GetMapping(value = "/week")
    public String getBestItemByWeek(Model model){
        List<BestItemDto> items = bestItemService.getBestOfWeekItem();

        model.addAttribute("title", "주간");
        model.addAttribute("items", items);

        return "item/bestItem";
    }
    
    /**
     * 월간 베스트 상품 페이지
     *
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 월간 베스트 상품 페이지 뷰 경로
     */
    @Operation(summary = "월간 베스트 상품 페이지", description = "월간 베스트 상품 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "월간 베스트 상품 페이지 뷰")
    })
    @GetMapping(value = "/month")
    public String getBestItemByMonth(Model model){
        List<BestItemDto> items = bestItemService.getBestOfMonthItem();

        model.addAttribute("title", "월간");
        model.addAttribute("items", items);

        return "item/bestItem";
    }

}