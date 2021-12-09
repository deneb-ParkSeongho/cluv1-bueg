package com.shop.controller;

import com.shop.dto.*;
import com.shop.entity.Category;
import com.shop.entity.UsedItem;
import com.shop.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

/**
 * 메인 컨트롤러
 *
 * @author 공통
 * @version 1.0
 */
@Tag(name = "메인", description = "메인 페이지 요청 처리")
@Controller
@RequiredArgsConstructor
public class MainController {

    private final CategoryService categoryService;

    private final BestItemService bestItemService;

    private final ReverseAuctionService reverseAuctionService;

    private final UsedItemService usedItemService;

    /**
     * 메인 페이지
     *
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return main 메인페이지 뷰 경로
     */
    @Operation(summary = "메인 페이지", description = "메인 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메인 페이지 뷰")
    })
    @GetMapping(value = "/")
    public String main(Model model) {
        Pageable pageable = PageRequest.of(0, 5);

        List<BestItemDto> bestItemList = bestItemService.getBestOfDayItem();

        Page<ReverseAuctionDto> reverseAuctionPages = reverseAuctionService.getUserReverseAuctionPage(new ReverseAuctionSearchDto(), pageable);
        List<ReverseAuctionDto> reverseAuctionList = reverseAuctionPages.getContent();

        Page<UsedItemDto> usedItemPages = usedItemService.getAllUsedItemPage(new UsedItemSearchDto(), pageable);
        List<UsedItemDto> usedItemList = usedItemPages.getContent();

        List<Category> categoryList = categoryService.getCategoryList();

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("bestItemList", bestItemList);
        model.addAttribute("reverseAuctionList", reverseAuctionList);
        model.addAttribute("usedItemList", usedItemList);

        return "main";
    }

}
