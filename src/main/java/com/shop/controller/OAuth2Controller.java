package com.shop.controller;

import com.shop.constant.Bank;
import com.shop.constant.OAuth2ProviderType;
import com.shop.dto.OAuth2FormDto;
import com.shop.entity.OAuth2Member;
import com.shop.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

/**
 * 주문 컨트롤러
 *
 * @author 유한종
 * @version 1.0
 */
@Tag(name = "OAuth2", description = "OAuth2 관련 요청 처리")
@RequestMapping("/oauth2")
@Controller
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    /**
     * OAuth2 로그인 리다이렉션
     *
     * @param code 서비스 제공자 코드
     * @param session 세션 객체
     *
     * @return OAuth2 로그인 리다이렉션
     */
    @Operation(summary = "OAuth2 로그인 리다이렉션", description = "OAuth2 로그인 리다이렉션 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 로그인 리다이렉션")
    })
    @GetMapping(value = "/login/{code}")
    public String oauthRedirect(@PathVariable("code") String code, HttpSession session) {
        OAuth2ProviderType providerType = OAuth2ProviderType.valueOf(code.toUpperCase());
        OAuth2ServiceType service = oAuth2Service.getProviderService(providerType);

        if(service instanceof GoogleOAuth2Service || service instanceof KakaoOAuth2Service) {
            String state = new BigInteger(130, new SecureRandom()).toString(32);

            session.setAttribute("state", state);

            if(service instanceof GoogleOAuth2Service) {
                GoogleOAuth2Service googleOAuth2Service = (GoogleOAuth2Service) service;

                return "redirect:" + googleOAuth2Service.getRedirectURL(state);
            } else {
                KakaoOAuth2Service kakaoOAuth2Service = (KakaoOAuth2Service) service;

                return "redirect:" + kakaoOAuth2Service.getRedirectURL(state);
            }
        }

        return "redirect:" + oAuth2Service.getRedirectURL(providerType);
    }

    /**
     * OAuth2 로그인 콜백 처리
     *
     * @param code 서비스 제공자 코드
     * @param paramMap 사용자 전달 파라메터 맵 객체
     * @param model 뷰에 전달할 모델 객체
     * @param session 세션 객체
     *
     * @return 성공: 메인 페이지 리다이렉션<br>
     *         성공: 추가 정보 입력 페이지 뷰 경로<br>
     *         실패: 에러 메시지
     */
    @Operation(summary = "OAuth2 로그인 콜백 처리", description = "OAuth2 로그인 콜백 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메인 페이지 리다이렉션<br>또는, 추가 정보 입력 페이지 뷰"),
            @ApiResponse(responseCode = "400", description = "에러 메시지")
    })
    @GetMapping(value = "/callback/{code}")
    public String oauthCallback(@PathVariable("code") String code, @RequestParam Map<String,String> paramMap, Model model, HttpSession session) throws Exception {
        OAuth2ProviderType providerType = OAuth2ProviderType.valueOf(code.toUpperCase());
        OAuth2ServiceType service = oAuth2Service.getProviderService(providerType);

        String authorizationCode = paramMap.get("code");
        String accessToken;

        if(service instanceof NaverOAuth2Service) {
            String stateCode = paramMap.get("state");

            accessToken = ((NaverOAuth2Service) service).getToken(stateCode, authorizationCode);
        } else {
            if(service instanceof GoogleOAuth2Service || service instanceof KakaoOAuth2Service) {
                String stateCode = paramMap.get("state");

                if(!stateCode.equals(session.getAttribute("state"))) {
                    throw new IllegalStateException("정상적인 접근이 아닙니다.");
                }
            }

            accessToken = service.getToken(authorizationCode);
        }

        if(accessToken == null) {
            throw new IllegalStateException("토큰을 발급받을 수 없습니다.");
        }

        Map<String, String> userInfo = service.getUserInfo(accessToken);

        String email = userInfo.get("email");
        String name = userInfo.get("name");

        if(email == null || name == null || email.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("이메일이나 이름을 확인할 수 없습니다.");
        }

        Authentication authentication = oAuth2Service.getAuthentication(email);

        if(authentication == null) {
            OAuth2FormDto oAuth2FormDto = new OAuth2FormDto();
            oAuth2FormDto.setName(name);
            oAuth2FormDto.setEmail(email);

            model.addAttribute("OAuth2FormDto", oAuth2FormDto);
            model.addAttribute("banks", Bank.values());
            model.addAttribute("code", code);

            session.setAttribute("OAuth2FormDto", oAuth2FormDto);

            return "member/memberSocialForm";
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/";
    }

    /**
     * OAuth2 회원 등록 처리
     *
     * @param code 서비스 제공자 코드
     * @param oAuth2FormDto 서비스 제공자 코드
     * @param bindingResult 사용자 입력값 오류 정보 객체
     * @param model 뷰에 전달할 모델 객체
     * @param session 세션 객체
     *
     * @return 성공: 메인 페이지 리다이렉션<br>
     *         실패: 추가 정보 입력 페이지 뷰 경로
     */
    @Operation(summary = "OAuth2 회원 등록 처리", description = "OAuth2 회원 등록 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 메인 페이지 리다이렉션<br>실패: 추가 정보 입력 페이지 뷰"),
    })
    @PostMapping(value = "/new/{code}")
    public String oauthForm(@PathVariable("code") String code, @Valid OAuth2FormDto oAuth2FormDto, BindingResult bindingResult, Model model, HttpSession session) {
        OAuth2ProviderType providerType = OAuth2ProviderType.valueOf(code.toUpperCase());

        model.addAttribute("code", code);
        model.addAttribute("banks", Bank.values());

        if(bindingResult.hasErrors()) {
            return "member/memberSocialForm";
        }

        try {
            OAuth2FormDto originalFormDto = (OAuth2FormDto) session.getAttribute("OAuth2FormDto");

            originalFormDto.setAddress(oAuth2FormDto.getAddress());
            originalFormDto.setAddressDetail(oAuth2FormDto.getAddressDetail());
            originalFormDto.setPhone(oAuth2FormDto.getPhone());
            originalFormDto.setRefundBank(oAuth2FormDto.getRefundBank());
            originalFormDto.setRefundAccount(oAuth2FormDto.getRefundAccount());

            if(providerType.equals(OAuth2ProviderType.GOOGLE)) {
                originalFormDto.setName(oAuth2FormDto.getName());
            }

            OAuth2Member oAuth2Member = OAuth2Member.createOAuth2Member(originalFormDto, providerType);

            oAuth2Service.saveOAuth2User(oAuth2Member);

            Authentication authentication = oAuth2Service.getAuthentication(oAuth2Member);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch(IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());

            return "member/memberSocialForm";
        }

        return "redirect:/";
    }

}
