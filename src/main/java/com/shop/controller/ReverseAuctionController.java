package com.shop.controller;

import com.shop.dto.*;
import com.shop.entity.ReverseAuction;
import com.shop.repository.ReverseAuctionRepository;
import com.shop.service.ItemService;
import com.shop.service.ReverseAuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 역경매 컨트롤러
 *
 * @author 유한종
 * @version 1.0
 */
@Tag(name = "역경매", description = "역경매 요청 처리")
@Controller
@RequiredArgsConstructor
public class ReverseAuctionController {

    private final ItemService itemService;

    private final ReverseAuctionService reverseAuctionService;

    private final ReverseAuctionRepository reverseAuctionRepository;

    /**
     * 역경매 목록 조회 페이지
     *
     * @param reverseAuctionSearchDto 검색 필드 정보
     * @param page 페이징 번호
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 역경매 목록 조회 뷰 경로
     */
    @Operation(summary = "역경매 목록 조회 페이지", description = "역경매 목록 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역경매 목록 조회 뷰")
    })
    @GetMapping(value = {"/rauctions", "/rauctions/{page}"})
    public String reverseAuctionList(@Parameter(description = "검색 필드 정보") ReverseAuctionSearchDto reverseAuctionSearchDto,
                                     @Parameter(description = "페이징 번호") @PathVariable("page") Optional<Integer> page,
                                     Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<ReverseAuctionDto> reverseAuctionDtoList = reverseAuctionService.getUserReverseAuctionPage(reverseAuctionSearchDto, pageable);
        List<ReverseAuctionHistoryDto> previousReverseAuctionDtoList = reverseAuctionService.getPreviousReverseAuctionPage();

        model.addAttribute("reverseAuctionDtoList", reverseAuctionDtoList);
        model.addAttribute("reverseAuctionSearchDto", reverseAuctionSearchDto);
        model.addAttribute("previousReverseAuctionDtoList", previousReverseAuctionDtoList);
        model.addAttribute("maxPage", 6);

        return "reverseAuction/reverseAuctionList";
    }

    /**
     * 역경매 상세 정보 조회 페이지
     *
     * @param reverseAuctionId 역경매 ID
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 역경매 상세 정보 조회 뷰 경로
     */
    @Operation(summary = "역경매 상세 정보 조회 페이지", description = "역경매 상세 정보 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역경매 상세 정보 조회 뷰")
    })
    @GetMapping(value = "/rauction/{reverseAuctionId}")
    public String reverseAuctionDtl(@Parameter(description = "역경매 ID") @PathVariable("reverseAuctionId") Long reverseAuctionId, Model model) {
        ReverseAuctionDto reverseAuctionDto = reverseAuctionService.getReverseAuctionDtl(reverseAuctionId);

        ReverseAuction reverseAuction = reverseAuctionRepository.findById(reverseAuctionId).orElseThrow(EntityNotFoundException::new);

        ItemFormDto itemFormDto = itemService.getItemDtl(reverseAuction.getItem().getId());

        model.addAttribute("reverseAuctionDto", reverseAuctionDto);
        model.addAttribute("itemFormDto", itemFormDto);

        return "reverseAuction/reverseAuctionDtl";
    }
    
    /**
     * 관리자 역경매 목록 조회 페이지
     *
     * @param reverseAuctionSearchDto 검색 필드 정보
     * @param page 페이징 번호
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 관리자 역경매 목록 조회 페이지 뷰 경로
     */
    @Operation(summary = "관리자 역경매 목록 조회 페이지", description = "관리자 역경매 목록 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 역경매 목록 조회 뷰")
    })
    @GetMapping(value = {"/admin/rauctions", "/admin/rauctions/{page}"})
    public String reverseAuctionManage(@Parameter(description = "검색 필드 정보") ReverseAuctionSearchDto reverseAuctionSearchDto,
                                       @Parameter(description = "페이징 번호") @PathVariable("page") Optional<Integer> page,
                                       Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);
        Page<ReverseAuctionHistoryDto> reverseAuctionDtoList = reverseAuctionService.getAdminReverseAuctionPage(reverseAuctionSearchDto, pageable);

        model.addAttribute("reverseAuctionDtoList", reverseAuctionDtoList);
        model.addAttribute("reverseAuctionSearchDto", reverseAuctionSearchDto);
        model.addAttribute("maxPage", 5);

        return "reverseAuction/reverseAuctionMng";
    }

    /**
     * 관리자 역경매 등록 페이지
     *
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 관리자 역경매 등록 페이지 뷰 경로
     */
    @Operation(summary = "관리자 역경매 등록 페이지", description = "관리자 역경매 등록 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 역경매 등록 뷰")
    })
    @GetMapping(value = "/admin/rauction/new")
    public String reverseAuctionForm(Model model) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        ItemSearchDto itemSearchDto = new ItemSearchDto();

        Page<MainItemDto> itemList = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("reverseAuctionFormDto", new ReverseAuctionFormDto());
        model.addAttribute("itemList", itemList);

        return "reverseAuction/reverseAuctionForm";
    }

    /**
     * 관리자 역경매 등록 처리
     *
     * @param reverseAuctionFormDto 사용자 입력 역경매 정보 객체
     * @param bindingResult 사용자 입력값 오류 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공: 관리자 역경매 목록 조회 페이지 뷰 경로
     *         실패: 관리자 역경매 등록 페이지 뷰 경로
     */
    @Operation(summary = "관리자 역경매 등록 처리", description = "관리자 역경매 등록 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 관리자 역경매 목록 조회 리다이렉션<br>실패: 관리자 역경매 등록 뷰"),
    })
    @PostMapping(value = "/admin/rauction/new")
    public String reverseAuctionNew(@Parameter(description = "사용자 입력 역경매 정보 객체") @Valid ReverseAuctionFormDto reverseAuctionFormDto,
                                    @Parameter(description = "사용자 입력값 오류 정보 객체") BindingResult bindingResult,
                                    Model model) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        ItemSearchDto itemSearchDto = new ItemSearchDto();

        Page<MainItemDto> itemList = itemService.getMainItemPage(itemSearchDto, pageable);

        if(bindingResult.hasErrors()) {
            model.addAttribute("itemList", itemList);

            return "reverseAuction/reverseAuctionForm";
        }

        try {
            reverseAuctionService.saveReverseAuction(reverseAuctionFormDto);
        } catch(Exception e) {
            model.addAttribute("itemList", itemList);
            model.addAttribute("errorMessage", "역경매 상품 등록 중 에러가 발생하였습니다.");

            return "reverseAuction/reverseAuctionForm";
        }

        return "redirect:/admin/rauctions";
    }

    /**
     * 관리자 역경매 수정 페이지
     *
     * @param reverseAuctionId 역경매 ID
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 관리자 역경매 수정 페이지 뷰 경로
     */
    @Operation(summary = "관리자 역경매 수정 페이지", description = "관리자 역경매 수정 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 관리자 역경매 수정 뷰<br>실패: 관리자 역경매 등록 뷰")
    })
    @GetMapping(value = "/admin/rauction/{reverseAuctionId}")
    public String reverseAuctionForm(@Parameter(description = "역경매 ID") @PathVariable("reverseAuctionId") Long reverseAuctionId, Model model) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        ItemSearchDto itemSearchDto = new ItemSearchDto();

        Page<MainItemDto> itemList = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("itemList", itemList);

        try {
            ReverseAuctionFormDto reverseAuctionFormDto = reverseAuctionService.getReserveAuctionForm(reverseAuctionId);

            model.addAttribute("reverseAuctionFormDto", reverseAuctionFormDto);
        } catch(Exception e) {
            model.addAttribute("reverseAuctionFormDto", new ReverseAuctionFormDto());
            model.addAttribute("errorMessage", "존재하지 않는 상품 입니다.");

            return "reverseAuction/reverseAuctionForm";
        }

        return "reverseAuction/reverseAuctionForm";
    }

    /**
     * 관리자 역경매 수정 처리
     *
     * @param reverseAuctionFormDto 사용자 입력 역경매 정보 객체
     * @param bindingResult 사용자 입력값 오류 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공: 관리자 역경매 목록 조회 페이지 뷰 경로
     *         실패: 관리자 역경매 수정 페이지 뷰 경로
     */
    @Operation(summary = "관리자 역경매 수정 처리", description = "관리자 역경매 수정 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 관리자 역경매 목록 조회 리다이렉션<br>실패: 관리자 역경매 수정 뷰")
    })
    @PostMapping(value = "/admin/rauction/{reverseAuctionId}")
    public String reverseAuctionUpdate(@Parameter(description = "사용자 입력 역경매 정보 객체") @Valid ReverseAuctionFormDto reverseAuctionFormDto,
                                       @Parameter(description = "사용자 입력값 오류 정보 객체") BindingResult bindingResult,
                                       Model model) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        ItemSearchDto itemSearchDto = new ItemSearchDto();

        Page<MainItemDto> itemList = itemService.getMainItemPage(itemSearchDto, pageable);

        if(bindingResult.hasErrors()) {
            model.addAttribute("itemList", itemList);

            return "reverseAuction/reverseAuctionForm";
        }

        try {
            reverseAuctionService.updateReserveAuction(reverseAuctionFormDto);
        } catch(Exception e) {
            model.addAttribute("itemList", itemList);
            model.addAttribute("errorMessage", "역경매 상품 수정 중 에러가 발생하였습니다.");

            return "reverseAuction/reverseAuctionForm";
        }

        return "redirect:/admin/rauctions";
    }

    /**
     * 관리자 역경매 삭제 처리
     *
     * @param reverseAuctionId 역경매 ID
     *
     * @return 관리자 역경매 목록 조회 페이지 뷰 경로
     */
    @Operation(summary = "관리자 역경매 삭제 처리", description = "관리자 역경매 삭제 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 역경매 목록 조회 리다이렉션")
    })
    @GetMapping(value = "/admin/rauction/{reverseAuctionId}/delete")
    public String reverseAuctionDelete(@Parameter(description = "역경매 ID") @PathVariable("reverseAuctionId") Long reverseAuctionId) {
        reverseAuctionService.deleteReverseAuction(reverseAuctionId);

        return "redirect:/admin/rauctions";
    }

}
