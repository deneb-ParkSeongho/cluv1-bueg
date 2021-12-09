package com.shop.controller;

import com.shop.dto.CommentFormDto;
import com.shop.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

/**
 * 문의사항 답글 컨트롤러
 *
 * @author 함형연
 * @version 1.0
 */
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 문의 사항 답글 추가 메소드
     *
     * @param inquiryId 답글을 추가할 문의사항 id
     * @param commentFormDto 답글 정보를 담을 객체
     *
     * @return 현재 페이지 리다이렉트
     */
    @PostMapping(value = "/admin/cscenter/voclist/{id}")
    public String saveComment(@PathVariable("id") Long inquiryId, @Valid CommentFormDto commentFormDto, BindingResult bindingResult, Model model) {
        commentService.saveComment(commentFormDto, inquiryId);

        return "redirect:/admin/cscenter/voclist/{id}";
    }

}
