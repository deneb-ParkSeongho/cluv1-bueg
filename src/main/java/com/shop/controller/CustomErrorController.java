package com.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * 예외 처리 컨트롤러
 *
 * @author 공통
 * @version 1.0
 */
@Tag(name = "예외 처리", description = "오류 요청 처리")
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * 에러 페이지
     *
     * @param request 사용자 요청 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 에러 페이지 뷰 경로
     */
    @Operation(summary = "에러 페이지", description = "에러 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "에러 페이지 뷰")
    })
    @RequestMapping(value = "/error")
    public String error(HttpServletRequest request, Model model) {
        Object statusCodeObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if(statusCodeObj != null) {
            int statusCode = Integer.valueOf(statusCodeObj.toString());

            model.addAttribute("statusCode", statusCode);
        }

        return "error";
    }

}