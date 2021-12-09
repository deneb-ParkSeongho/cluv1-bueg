package com.shop.controller;

import com.shop.dto.CommentFormDto;
import com.shop.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "문의사항 답글", description = "문의사항 답글 요청 처리")
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
    @Operation(summary = "문의 사항 답글 추가 메소드", description = "문의 사항 답글 추가 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 페이지 리다이렉트")
    })
    @PostMapping(value = "/admin/cscenter/voclist/{id}")
    public String saveComment(@Parameter(description = "답글을 추가할 문의사항 id") @PathVariable("id") Long inquiryId,
                              @Parameter(description = "답글 정보를 담을 객체") @Valid CommentFormDto commentFormDto,
                              BindingResult bindingResult,
                              Model model) {
        commentService.saveComment(commentFormDto, inquiryId);

        return "redirect:/admin/cscenter/voclist/{id}";
    }

}
