package com.shop.controller;

import com.shop.dto.OrderHistDto;
import com.shop.dto.ReviewFormDto;
import com.shop.service.OrderService;
import com.shop.service.ReviewImgService;
import com.shop.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * 리뷰 컨트롤러
 * @author 강은별
 * @version 1.0
 *
 */

@Controller
@RequiredArgsConstructor
@Tag(name = "리뷰 컨트롤러", description = "리뷰 컨트롤러 목록")
public class ReviewController {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ReviewImgService reviewImgService;

    /**
     *
     * @param page 페이징을 하기 위한 객체
     * @param principal 현재 로그인 중인 회원 정보를 불러오기 위한 객체
     * @param model 생성한 데이터들을 view로 전달하기 위한 객체
     * @return 리뷰 내역을 보여주는 view 반환
     */
    @GetMapping(value = {"/reviews", "reviews/{page}"})
    public String reviews(@PathVariable("page") Optional<Integer> page, Principal principal, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4);
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "review/reviewList";
    }

    /**
     *
     * @param orderItemId 주문 번호가 담겨있는 파라미터
     * @param model 생성한 데이터들을 view로 전달하기 위한 객체
     * @return 리뷰 작성 폼 반환
     */
    @GetMapping("/reviews/new/{itemId}")
    public String reviewForm(@PathVariable("itemId") Long orderItemId ,Model model) {
        ReviewFormDto reviewFormDto = new ReviewFormDto();
        reviewFormDto.setReviewId(orderItemId);

        model.addAttribute("reviewFormDto", reviewFormDto);
        model.addAttribute("reviewFormType", "WRITE");

        return "review/reviewForm";
    }

    /**
     *
     * @param orderItemId 주문 번호가 담겨있는 파라미터
     * @param reviewFormDto 리뷰 내용을 담는 객체
     * @param bindingResult 바인딩 에러 결과
     * @param reviewImgFile 리뷰 사진을 담을 리스트
     * @param model 생성한 데이터들을 view로 전달하기 위한 객체
     * @return 에러 없이 작동 시 주문 내역 view로 redirect
     */
    @PostMapping("/reviews/new/{itemId}")
    public String reviewNew(@PathVariable("itemId") Long orderItemId, @Valid ReviewFormDto reviewFormDto, BindingResult bindingResult, @RequestParam("reviewImgFile") List<MultipartFile> reviewImgFile, Model model) {
        model.addAttribute("reviewFormType", "WRITE");

        reviewFormDto.setReviewId(orderItemId);

        if(bindingResult.hasErrors()) {
            return "review/reviewForm";
        }

        if(reviewImgFile.get(0).isEmpty()) {
            model.addAttribute("errorMessage", "사진 등록은 필수 입력 값입니다.");

            return "review/reviewForm";
        }

        if(reviewFormDto.getComment().length() == 0) {
            model.addAttribute("errorMessage", "리뷰 작성은 필수 입력 값입니다.");

            return "review/reviewForm";
        }

        try {
            reviewService.saveReview(orderItemId, reviewFormDto, reviewImgFile);
        } catch(Exception e) {
            model.addAttribute("errorMessage", "리뷰 작성 중 에러가 발생하였습니다.");

            return "review/reviewForm";
        }

        return "redirect:/orders";
    }

    /**
     *
     * @param orderItemId 주문 번호가 담겨있는 파라미터
     * @param model 생성한 데이터들을 view로 전달하기 위한 객체
     * @return 리뷰 수정을 위한 리뷰 작성 폼 반환
     */
    @GetMapping("/reviews/update/{itemId}")
    public String reviewDtl(@PathVariable("itemId") Long orderItemId, Model model) {
        try {
            ReviewFormDto reviewFormDto = reviewService.getReviewDtl(orderItemId);
            reviewFormDto.setReviewId(orderItemId);

            model.addAttribute("reviewFormDto", reviewFormDto);
            model.addAttribute("reviewFormType", "UPDATE");
        } catch(EntityNotFoundException e) {
            model.addAttribute("errorMessage", "후기를 작성하지 않았습니다.");
            model.addAttribute("reviewFormDto", new ReviewFormDto());
            model.addAttribute("reviewFormType", "WRITE");

            return "review/reviewForm";
        }

        return "review/reviewForm";
    }

    /**
     *
     * @param orderItemId 주문 번호가 담겨있는 파라미터
     * @param reviewFormDto 수정된 리뷰 내용을 담는 객체
     * @param bindingResult 바인딩 에러 결과
     * @param reviewImgFile 수정된 리뷰 사진을 담을 리스트
     * @param model 생성한 데이터들을 view로 전달하기 위한 객체
     * @return 에러 없이 작동 시 주문 내역 view로 redirect
     */
    @PostMapping("/reviews/update/{itemId}")
    public String reviewUpdate(@PathVariable("itemId") Long orderItemId, @Valid ReviewFormDto reviewFormDto, BindingResult bindingResult, @RequestParam("reviewImgFile") List<MultipartFile> reviewImgFile, Model model) {
        model.addAttribute("reviewFormType", "UPDATE");

        reviewFormDto.setReviewId(orderItemId);

        if(bindingResult.hasErrors()) {
            return "review/reviewForm";
        }

        if(reviewFormDto.getComment() == null) {
            model.addAttribute("errorMessage", "후기 작성은 필수 입력 값입니다.");

            return "review/reviewForm";
        }

        try {
            reviewService.updateReview(orderItemId, reviewFormDto, reviewImgFile);
        } catch(Exception e) {
            model.addAttribute("errorMessage", "리뷰 작성 중 에러가 발생하였습니다.");

            return "review/reviewForm";
        }

        return "redirect:/orders";
    }

    /**
     *
     * @param orderItemId 주문 번호가 담겨있는 파라미터
     * @param reviewFormDto 리뷰 내용을 담는 객체
     * @return 리뷰 삭제 후 주문 내역 view로 redirect
     */
    @GetMapping("/reviews/delete/{itemId}")
    public String reviewDelete(@PathVariable("itemId") Long orderItemId, ReviewFormDto reviewFormDto) {
        reviewService.deleteReview(orderItemId, reviewFormDto);
        reviewImgService.deleteReviewImg(orderItemId);

        return "redirect:/orders";
    }

}
