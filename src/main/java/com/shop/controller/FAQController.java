package com.shop.controller;

import com.shop.dto.FAQDto;
import com.shop.dto.FAQSearchDto;
import com.shop.service.FAQService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * FAQ 컨트롤러
 *
 * @author 함형연
 * @version 1.0
 */
@Controller
@RequiredArgsConstructor
public class FAQController {

    private final FAQService faqService;

    /**
     * faq 리스트를 조회하는 메소드
     *
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return faq 페이지 반환
     */
    @GetMapping(value = "/cscenter")
    public String readFaq(Model model) {
        List<FAQDto> FAQDtoList = faqService.getFAQList();

        model.addAttribute("faqSearchDto",new FAQSearchDto());
        model.addAttribute("FAQList", FAQDtoList);

        return "cscenter/faq";
    }

    /**
     * admin계정에서 faq 리스트를 조회하는 메소드
     *
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return adminfaq 페이지 반환
     */
    @GetMapping(value = "/admin/cscenter")
    public String adminReadFaq(Model model) {
        List<FAQDto> FAQDtoList = faqService.getFAQList();

        model.addAttribute("faqsearchdto", new FAQSearchDto());
        model.addAttribute("FAQList", FAQDtoList);

        return "cscenter/adminfaq";
    }

    /**
     * question으로 faq를 검색한 결과 조회 메소드
     *
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return faqsearch 페이지 반환
     */
    @PostMapping(value = "/cscenter/search")
    public String search(FAQSearchDto faqSearchDto, Model model) {
        List<FAQDto> FAQDtoList = faqService.getSearchResult(faqSearchDto);

        model.addAttribute("faqsearchdto",new FAQSearchDto());
        model.addAttribute("FAQDtoList",FAQDtoList);

        return "cscenter/faqsearch";
    }

    /**
     * admin계정에서 question으로 faq를 검색한 결과 조회 메소드
     *
     * @param faqSearchDto 검색어를 담은 객체
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return adminfaqsearch 페이지 반환
     */
    @PostMapping(value = "/admin/cscenter/search")
    public String adminSearch(FAQSearchDto faqSearchDto, Model model) {
        List<FAQDto> FAQDtoList = faqService.getSearchResult(faqSearchDto);

        model.addAttribute("faqsearchdto",new FAQSearchDto());
        model.addAttribute("FAQDtoList",FAQDtoList);

        return "cscenter/adminfaqsearch";
    }

}
