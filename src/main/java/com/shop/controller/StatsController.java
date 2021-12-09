package com.shop.controller;

import com.shop.repository.EmailNoticeRepository;
import com.shop.repository.SmsNoticeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 알림 통계 컨트롤러
 *
 * @author 조우진
 * @version 1.0
 */
@Tag(name = "알림 통계", description = "알림 통계 관련 요청 처리")
@Controller
@RequiredArgsConstructor
public class StatsController {

    private final EmailNoticeRepository emailNoticeRepository;
    private final SmsNoticeRepository smsNoticeRepository;

    /**
     * 알림 통계 페이지
     *
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 알림 통계 뷰 경로
     */
    @Operation(summary = "알림 통계 페이지", description = "알림 통계 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 통계 페이지 뷰"),
    })
    @GetMapping(value= "/admin/noticeStats")
    public String stats(Model model) {
        long emailCount = emailNoticeRepository.count();
        long smsCount = smsNoticeRepository.count();

        model.addAttribute("emailCount", emailCount);
        model.addAttribute("smsCount", smsCount);

        return "/notice/noticeStats";
    }

}
