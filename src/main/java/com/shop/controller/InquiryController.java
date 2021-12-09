package com.shop.controller;

import com.shop.dto.CommentFormDto;
import com.shop.dto.FAQSearchDto;
import com.shop.dto.InquiryFormDto;
import com.shop.service.CommentService;
import com.shop.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 문의사항 컨트롤러
 *
 * @author 함형연
 * @version 1.0
 */
@Controller
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final CommentService commentService;

    /**
     * 문의 사항을 입력받는 폼을 조회하는 메소드
     *
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return inquiryForm 페이지 반환
     */
    @GetMapping(value = "/cscenter/voc")
    public String saveInquiryForm(Model model) {
        model.addAttribute("inquiryFormDto",new InquiryFormDto());
        model.addAttribute("faqsearchdto",new FAQSearchDto());

        return "cscenter/inquiryForm";
    }

    /**
     * 입력받은 문의 사항을 저장하는 메소드
     *
     * @param inquiryFormDto 입력받은 문의사항의 정보를 담은 객체
     * @param bindingResult 바인딩의 에러 결과
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return voclist페이지 리다이렉트
     */
    @PostMapping(value="/cscenter/voc")
    public String saveInquiry(@Valid InquiryFormDto inquiryFormDto, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "cscenter/inquiryForm";
        }

        try {
            inquiryService.saveInquiry(inquiryFormDto);
        } catch(IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());

            return "cscenter/inquiryForm";
        }

        return "redirect:/cscenter/voclist";
    }

    /**
     * 현재 로그인한 멤버의 문의 사항 리스트를 조회하는 메소드
     *
     * @param principal 현재 로그인한 회원
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return voclist페이지 리다이렉트
     */
    @GetMapping(value = "/cscenter/voclist")
    public String readInquiry(Model model, Principal principal) {
        List<InquiryFormDto> InquiryList = inquiryService.getInquiryList(principal.getName());

        model.addAttribute("InquiryList", InquiryList);
        model.addAttribute("faqsearchdto", new FAQSearchDto());

        return "cscenter/vocList";
    }

    /**
     * 선택한 문의사항의 세부사항을 보여주는 메서드
     *
     * @param id 세부사항을 볼 문의사항 id
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return voclist페이지 리다이렉트
     */
    @GetMapping(value = "/cscenter/voclist/{id}")
    public String readDetailInquiry(@PathVariable("id") Long id, Model model) {
        InquiryFormDto inquiryFormDto = inquiryService.getInquiryDtl(id);

        List<CommentFormDto> commentFormDto = commentService.getCommentList(id);

        model.addAttribute("faqsearchdto",new FAQSearchDto());
        model.addAttribute("inquiry", inquiryFormDto);
        model.addAttribute("commentlist",commentFormDto);

        return "cscenter/inquiryDtl";
    }

    /**
     * 선택한 문의사항의 수정 폼을 조화하는 메소드
     *
     * @param id 수정할 문의사항 id
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return voclist페이지 리다이렉트
     */
    @GetMapping(value = "/cscenter/voclist/edit/{id}")
    public String modifyInquiryForm(@PathVariable("id") Long id, Model model) {
        InquiryFormDto inquiryFormDto = inquiryService.getInquiryDtl(id);

        model.addAttribute("inquiryFormDto", inquiryFormDto);
        model.addAttribute("faqsearchdto", new FAQSearchDto());

        return "cscenter/inquiryEdit";
    }

    /**
     * 문의사항을 수정하는 메소드
     *
     * @param inquiryFormDto 문의사항을 수정한 정보를 담은 객체
     *
     * @return voclist페이지 리다이렉트
     */
    @PostMapping(value = "/cscenter/voclist/edit/{id}")
    public String modifyInquiry(@Valid InquiryFormDto inquiryFormDto) {
        inquiryService.updateInquiry(inquiryFormDto);

        return "redirect:/cscenter/voclist/";
    }

    /**
     * 문의사항을 삭제하는 메소드
     *
     * @param inquiryIdxArray 삭제할 문의사항 ID를 담은 객체
     *
     * @return
     */
    @ResponseBody
    @DeleteMapping("/cscenter/voclist")
    public String removeInquiry(@RequestBody List<String> inquiryIdxArray) {
        List<Long> newList = inquiryIdxArray.stream().map(s -> Long.parseLong(s)).collect(Collectors.toList());

        for(int i = 0; i < inquiryIdxArray.size(); i++) {
            commentService.deleteComment(newList.get(i));
        }

        for(int i = 0; i < inquiryIdxArray.size(); i++) {
            inquiryService.deleteInquiry(newList.get(i));
        }

        return "";
    }

    /**
     * admin계정에서 모든 멤버의 문의 사항 리스트를 조회하는 메소드
     *
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return adminvocList페이지 반환
     */
    @GetMapping(value = "/admin/cscenter/voclist")
    public String adminReadInquiry(Model model) {
        List<InquiryFormDto> InquiryList = inquiryService.getAllInquiryList();

        model.addAttribute("faqsearchdto", new FAQSearchDto());
        model.addAttribute("InquiryList", InquiryList);

        return "cscenter/adminvocList";
    }

    /**
     * admin계정에서 선택한 문의사항의 세부사항을 보여주는 메소드
     *
     * @param id 세부사항을 볼 문의사항 id
     * @param model view로 전달할 데이터를 담은 객체
     *
     * @return admininquiryDtl페이지 반환
     */
    @GetMapping(value = "/admin/cscenter/voclist/{id}")
    public String adminReadDetailInquiry(@PathVariable("id") Long id, Model model) {
        InquiryFormDto inquiryFormDto = inquiryService.getInquiryDtl(id);

        List<CommentFormDto> commentFormDto = commentService.getCommentList(id);

        model.addAttribute("inquiry", inquiryFormDto);
        model.addAttribute("comment", new CommentFormDto());
        model.addAttribute("faqsearchdto", new FAQSearchDto());
        model.addAttribute("commentlist", commentFormDto);

        return "cscenter/admininquiryDtl";
    }

}
