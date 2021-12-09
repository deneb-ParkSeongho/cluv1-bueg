package com.shop.controller;

import com.shop.dto.*;
import com.shop.service.AddressService;
import com.shop.service.ItemService;
import com.shop.service.OrderService;
import com.shop.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

/**
 * 선물하기 컨트롤러
 *
 * @author Lychee
 * @version 1.0
 */

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "선물하기 컨트롤러", description = "선물하기 컨트롤러 목록")
public class GiftController {

    private final AddressService addressService;
    private final OrderService orderService;
    private final ItemService itemService;
    private final SmsService smsService;

    /**
     * 선물하기 카테고리 조회 메소드
     *
     * @param itemSearchDto 검색 상품 리스트를 담은 객체
     * @param cateCode 카테고리 아이디
     * @param page 해당 카테고리 상품 페이지
     * @param model 해당 카테고리 상품 Dto, 검색 상품 Dto, 최대 페이지 수
     *
     * @return "gift/giftMain" 선물하기 메인페이지로 반환
     */

    @Operation(summary = "선물하기 카테고리 조회 메소드", description = "선물하기 카테고리 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선물하기 카테고리 조회"),
            @ApiResponse(responseCode = "400", description = "선물하기 카테고리 조회 실패")})
    @GetMapping(value = "/giftMain/{cateCode}")
    public String giftMain(@Parameter(description = "카테고리 아이디")@PathVariable("cateCode") Long cateCode, ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0,6);
        Page<GiftMainItemDto> items = itemService.getGiftItemPage(itemSearchDto, pageable, cateCode);

        log.info(String.valueOf(cateCode));

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return "gift/giftMain";
    }

    /**
     * 선물하기 배송지 입력 문자 전송 메소드
     *
     * @param from 문자 발신자
     * @param text 문자 내용
     *
     * @return new ResponseEntity<Long>(HttpStatus.OK) http 응답 상태코드 반환
     */
    @Operation(summary = "선물하기 배송지 입력 문자 전송 메소드", description = "선물하기 배송지 입력 문자 전송")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선물하기 배송지 입력 문자 전송"),
            @ApiResponse(responseCode = "400", description = "선물하기 배송지 입력 문자 전송 실패")})
    @PostMapping(value = "/sendSms")
    public @ResponseBody ResponseEntity sendSms(@Parameter(description = "보낼 사람")@RequestParam(name = "from") String from, @Parameter(description = "문자내용") @RequestParam(name = "text") String text) {
        smsService.sendSms(from, text);

        log.info("문자 전송 완료");

        return new ResponseEntity<Long>(HttpStatus.OK);
    }

    /**
     * 선물 배송지 입력 페이지 조회
     *
     * @param itemId 상품 아이디
     * @param count 상품 주문 수량
     * @param model 선물하기 form Dto
     *
     * @return "gift/giftMain" 선물하기 메인페이지로 반환
     */
    @Operation(summary = "선물 배송지 입력 페이지 조회", description = "선물 배송지 입력 페이지")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선물 배송지 입력 페이지 조회"),
            @ApiResponse(responseCode = "400", description = "선물 배송지 입력 페이지 조회 실패")})
    // 선물하기 폼
    @GetMapping(value ="/giftForm/{itemId}")
    public String giftForm(@Parameter(description = "상품 아이디") @PathVariable("itemId") Long itemId,@Parameter(description = "상품 주문 수량") @RequestParam("count") Integer count, Model model) {
        GiftDto giftDto = new GiftDto();
        giftDto.setItemId(itemId);
        giftDto.setCount(count);

        model.addAttribute("giftDto", giftDto);

        return "gift/giftForm";
    }

    /**
     * 선물하기 메소드
     *
     * @param giftDto 선물 받는 분의 정보를 담은 객체
     * @param bindingResult 바인딩 결과
     * @param principal 현재 로그인한 회원 정보
     * @param model 선물 완료 알림 메시지, redirect location
     *
     * @return "gift/giftForm" 선물하기 form 페이지로 반환
     */
    @Operation(summary = "선물하기 메소드", description = "선물하기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선물하기 성공"),
            @ApiResponse(responseCode = "400", description = "선물하기 실패")})
    @PostMapping(value = "/gift")
    public String gift(@Valid GiftDto giftDto, BindingResult bindingResult, Principal principal, Model model) {
        if(bindingResult.hasErrors()) {
            return "gift/giftForm";
        }

        String email = principal.getName();

        try {
            orderService.order(giftDto.toOrderDto(), email);
            addressService.saveAddress(giftDto.toAddressDto(), email);
        } catch(Exception e) {
            log.error(e.getMessage(), e);

            return "gift/giftForm";
        }

        model.addAttribute("message", "선물하기가 완료되었습니다.");
        model.addAttribute("location", "/orders");

        return "redirect";
    }

}
