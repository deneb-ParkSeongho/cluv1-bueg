package com.shop.controller;

import com.shop.dto.NaverShopItemDto;
import com.shop.dto.UsedItemDto;
import com.shop.dto.UsedItemFormDto;
import com.shop.dto.UsedItemSearchDto;
import com.shop.service.NaverShopService;
import com.shop.service.UsedItemService;
import groovy.util.logging.Slf4j;
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
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * 중고 상품 컨트롤러
 *
 * @author 이연
 * @version 1.0
 */
@Tag(name = "중고 상품", description = "중고 상품 관련 요청 처리")
@Slf4j
@Controller
@RequiredArgsConstructor
public class UsedItemController {

    private final UsedItemService usedItemService;
    private final NaverShopService naverShopService;
    
    /**
     * 중고 상품 조회 페이지
     *
     * @param usedItemSearchDto 검색 필드 정보 객체
     * @param page 페이징 번호
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 중고 상품 조회 페이지 뷰 경로
     */
    @Operation(summary = "중고 상품 조회 페이지", description = "중고 상품 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중고 상품 조회 페이지 뷰")
    })
    @GetMapping(value = { "/uitems", "/uitems/{page}" })
    public String usedItemList(UsedItemSearchDto usedItemSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<UsedItemDto> usedItemList = usedItemService.getAllUsedItemPage(usedItemSearchDto, pageable);

        model.addAttribute("usedItemList", usedItemList);
        model.addAttribute("usedItemSearchDto", usedItemSearchDto);
        model.addAttribute("maxPage", 5);

        return "usedItem/usedItemList";
    }

    /**
     * 중고 상품 관리 페이지
     *
     * @param usedItemSearchDto 검색 필드 정보 객체
     * @param page 페이징 번호
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 중고 상품 관리 페이지 뷰 경로
     */
    @Operation(summary = "중고 상품 관리 페이지", description = "중고 상품 관리 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중고 상품 관리 페이지 뷰")
    })
    @GetMapping(value = { "/uitem/manage", "/uitem/manage/{page}" })
    public String usedItemMng(UsedItemSearchDto usedItemSearchDto, Optional<Integer> page, Principal principal, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<UsedItemDto> usedItemList = usedItemService.getUserUsedItemPage(principal.getName(), usedItemSearchDto, pageable);

        model.addAttribute("usedItemList", usedItemList);
        model.addAttribute("usedItemSearchDto", usedItemSearchDto);
        model.addAttribute("maxPage", 5);

        return "usedItem/usedItemMng";
    }

    /**
     * 중고 상품 등록 페이지
     *
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 중고 상품 등록 페이지 뷰 경로
     */
    @Operation(summary = "중고 상품 등록 페이지", description = "중고 상품 등록 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중고 상품 등록 페이지 뷰")
    })
    @GetMapping(value = "/uitem/new")
    public String usedItemForm(Model model) {
        model.addAttribute("usedItemFormDto", new UsedItemFormDto());

        return "usedItem/usedItemForm";
    }

    /**
     * 중고 상품 등록 처리
     *
     * @param usedItemFormDto 사용자 입력 중고 상품 정보 객체
     * @param bindingResult 사용자 입력값 오류 정보 객체
     * @param usedItemImgFileList 중고 상품 업로드 이미지 파일 리스트
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공: 중고 상품 조회 페이지 리다이렉션<br>실패: 중고 상품 등록 페이지 뷰 경로
     */
    @Operation(summary = "중고 상품 등록 처리", description = "중고 상품 등록 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 중고 상품 조회 페이지 리다이렉션<br>실패: 중고 상품 등록 페이지 뷰 경로")
    })
    @PostMapping(value = "/uitem/new")
    public String usedItemNew(@Valid UsedItemFormDto usedItemFormDto, BindingResult bindingResult, @RequestParam("usedItemImgFile") List<MultipartFile> usedItemImgFileList, Principal principal, Model model) {
        if(bindingResult.hasErrors()) {
            return "usedItem/usedItemForm";
        }

        if(usedItemImgFileList.get(0).isEmpty() && usedItemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");

            return "usedItem/usedItemForm";
        }

        String email = principal.getName();

        try {
            usedItemService.saveUsedItem(usedItemFormDto, usedItemImgFileList, email);
        } catch(Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");

            return "usedItem/usedItemForm";
        }

        return "redirect:/uitems";
    }

    /**
     * 중고 상품 수정 페이지
     *
     * @param usedItemId 중고 상품 ID
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공: 중고 상품 수정 페이지 뷰<br>실패: 메인 페이지 리다이렉션<br>실패: 중고 상품 등록 페이지 뷰
     */
    @Operation(summary = "중고 상품 수정 페이지", description = "중고 상품 수정 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 중고 상품 수정 페이지 뷰<br>실패: 메인 페이지 리다이렉션<br>실패: 중고 상품 등록 페이지 뷰")
    })
    @GetMapping(value = "/uitem/update/{usedItemId}")
    public String usedItemUpdateForm(@PathVariable("usedItemId") Long usedItemId, Principal principal, Model model) {
        if(!usedItemService.validateUsedItem(usedItemId, principal.getName())) {
            model.addAttribute("message", "수정할 권한이 없습니다.");
            model.addAttribute("location", "/");

            return "redirect";
        }

        try {
            UsedItemFormDto usedItemFormDto = usedItemService.getUsedItemDtl(usedItemId);

            model.addAttribute("usedItemFormDto", usedItemFormDto);
        } catch(EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 중고 상품 입니다.");
            model.addAttribute("usedItemFormDto", new UsedItemFormDto());

            return "usedItem/usedItemForm";
        }

        return "usedItem/usedItemForm";
    }

    /**
     * 중고 상품 수정 처리
     *
     * @param usedItemFormDto 사용자 입력 중고 상품 정보 객체
     * @param bindingResult 사용자 입력값 오류 정보 객체
     * @param usedItemImgFileList 중고 상품 업로드 이미지 파일 리스트
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공: 중고 상품 조회 페이지 리다이렉션<br>실패: 메인 페이지 리다이렉션<br>실패: 중고 상품 수정 페이지 뷰 경로
     */
    @Operation(summary = "중고 상품 수정 처리", description = "중고 상품 수정 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 중고 상품 조회 페이지 리다이렉션<br>실패: 메인 페이지 리다이렉션<br>실패: 중고 상품 수정 페이지 뷰 경로")
    })
    @PostMapping(value = "/uitem/update/{usedItemId}")
    public String usedItemUpdate(@Valid UsedItemFormDto usedItemFormDto, BindingResult bindingResult, @RequestParam("usedItemImgFile") List<MultipartFile> usedItemImgFileList, Principal principal, Model model) {
        if(!usedItemService.validateUsedItem(usedItemFormDto.getId(), principal.getName())) {
            model.addAttribute("message", "수정할 권한이 없습니다.");
            model.addAttribute("location", "/");

            return "redirect";
        }

        if(usedItemImgFileList.get(0).isEmpty() && usedItemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");

            return "usedItem/usedItemForm";
        }

        try {
            usedItemService.updateUsedItem(usedItemFormDto, usedItemImgFileList);
        } catch(Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");

            return "usedItem/usedItemForm";
        }

        return "redirect:/uitem/manage";
    }

    /**
     * 중고 상품 상세 보기 페이지
     *
     * @param usedItemId 중고 상품 ID
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 중고 상품 상세 보기 페이지 뷰 경로
     */
    @Operation(summary = "중고 상품 상세 보기 페이지", description = "중고 상품 상세 보기 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중고 상품 상세 보기 페이지 뷰")
    })
    @GetMapping(value = "/uitem/{usedItemId}")
    public String usedItemDtl(@PathVariable("usedItemId") Long usedItemId, Model model) {
        UsedItemFormDto usedItemFormDto = usedItemService.getUsedItemDtl(usedItemId);

        model.addAttribute("usedItem", usedItemFormDto);

        return "usedItem/usedItemDtl";
    }

    /**
     * 네이버 쇼핑 시세 정보 조회
     *
     * @param name 검색할 상품명을 담은 객체
     *
     * @return 네이버 쇼핑 시세 정보
     */
    @Operation(summary = "네이버 쇼핑 시세 조회", description = "네이버 쇼핑 시세 조회 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "네이버 쇼핑 시세 정보")
    })
    @GetMapping(value = "/uitem/naverShopItems")
    public @ResponseBody List<NaverShopItemDto> getMarketItems(@RequestParam("name") String name) {
        return naverShopService.search(name);
    }

}
