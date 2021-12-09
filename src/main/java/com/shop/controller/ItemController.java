package com.shop.controller;

import com.shop.dto.*;
import com.shop.entity.Item;
import com.shop.service.CategoryService;
import com.shop.service.ItemService;
import com.shop.service.ReviewService;
import com.shop.service.TagService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 상품 컨트롤러
 *
 * @author 공통
 * @version 1.0
 */
@Tag(name = "상품", description = "상품 관련 요청 처리")
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final TagService tagService;
    private final CategoryService categoryService;
    private final ItemService itemService;
    private final ReviewService reviewService;

    /**
     * 관리자 상품 등록 입력값 전달
     *
     * @param model 상품 정보 입력값을 뷰로 전달하는 객체
     *
     * @return 상품 등록 페이지 반환
     */
    @Operation(summary = "관리자 상품 등록 페이지", description = "관리자 상품 등록 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 상품 등록 페이지 뷰")
    })
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("tagList", tagService.getTagList());
        model.addAttribute("itemFormDto", new ItemFormDto());

        return "/item/itemForm";
    }

    /**
     * 관리자 상품 등록 입력값 확인
     *
     * @param itemFormDto 등록할 상품정보를 담는 객체
     * @param bindingResult 바인딩의 에러 결과
     * @param model 사용자가 입력한 상품정보에 대한 에러메세지가 나오는 객체
     *
     * @return 상품 등록 페이지 재호출
     */
    @Operation(summary = "관리자 상품 등록 처리", description = "관리자 상품 등록 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 관리자 상품 조회 리다이렉션<br>실패: 관리자 상품 등록 페이지 뷰")
    })
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList, Model model) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("tagList", tagService.getTagList());

        if(bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");

            return "item/itemForm";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch(Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");

            return "item/itemForm";
        }

        return "redirect:/admin/items";
    }

    /**
     * 관리자 상품 수정 페이지 이동
     *
     * @param itemId 수정할 상품정보를 담는 객체
     * @param model 엔티티가 존재하지 않을 경우와 존재하지 않는 상품에 대한 에러메세지를 담는 객체
     *
     * @return 상품 수정 페이지 호출
     */
    @Operation(summary = "관리자 상품 수정 페이지", description = "관리자 상품 수정 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 상품 수정 페이지 뷰")
    })
    @GetMapping(value = "/admin/item/{itemId}")
    public String itemFormUpdate(@PathVariable("itemId") Long itemId, Model model) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("tagList", tagService.getTagList());

        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);

            model.addAttribute("itemFormDto", itemFormDto);
        } catch(EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품 입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());

            return "item/itemForm";
        }

        return "item/itemForm";
    }

    /**
     * 관리자 상품 정보 수정
     *
     * @param itemFormDto 수정할 상품정보를 담는 객체
     * @param bindingResult 바인딩의 에러결과
     * @param model 수정할 상품이 null이거나 오류가 발생하면 에러메세지를 반환
     *
     * @return 상품 수정페이지 호출
     */
    @Operation(summary = "관리자 상품 수정 처리", description = "관리자 상품 수정 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 관리자 상품 조회 리다이렉션<br>실패: 관리자 상품 등록 페이지 뷰")
    })
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList, Model model) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("tagList", tagService.getTagList());

        if(bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");

            return "item/itemForm";
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch(Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");

            return "item/itemForm";
        }

        return "redirect:/admin/items";
    }

    /**
     * 관리자 상품 관리 화면 이동 및 조회한 상품 데이터를 화면에 전달
     *
     * @param itemSearchDto 상품 조회 조건을 담는 객체
     * @param model 상품 정보, 상품 조회 조건을 담는 객체
     *
     * @return 상품 등록페이지 호출
     */
    @Operation(summary = "관리자 상품 조회 페이지", description = "관리자 상품 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 상품 조회 페이지 뷰")
    })
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return "item/itemMng";
    }

    /**
     * 상품 상세 페이지 메소드
     *
     * @param itemId 상품 정보가 담겨있는 객체
     * @param model 조회한 상품 데이터를 뷰로 전달과 에러메세지를 담는 객체
     *
     * @return 상품 상세페이지 호출
     */
    @Operation(summary = "상품 상세 조회 페이지", description = "상품 상세 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 상세 조회 페이지 뷰")
    })
    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        List<ReviewItemDto> orderItemDtoList = reviewService.getReviewItem(itemId);
        List<ReviewImgDto> reviewImgDtoList = reviewService.getReviewItemImg(itemId);

        model.addAttribute("item", itemFormDto);
        model.addAttribute("orderItemList", orderItemDtoList);
        model.addAttribute("reviewImgDtoList", reviewImgDtoList);

        return "item/itemDtl";
    }

    @Operation(summary = "상품 통합 검색 페이지", description = "상품 통합 검색 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 통합 검색 페이지 뷰")
    })
    @GetMapping(value = { "/items", "/items/{page}" })
    public String itemList(ItemComplexSearchDto itemComplexSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 10);
        Page<MainItemDto> items = itemService.getComplexSearchPage(itemComplexSearchDto, pageable);

        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("tagList", tagService.getTagList());
        model.addAttribute("itemComplexSearchDto", itemComplexSearchDto);
        model.addAttribute("items", items);
        model.addAttribute("maxPage", 5);

        return "item/itemList";
    }

}
