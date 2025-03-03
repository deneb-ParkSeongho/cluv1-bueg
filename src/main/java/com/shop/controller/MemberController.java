package com.shop.controller;

import com.shop.constant.Bank;
import com.shop.dto.MemberFormDto;
import com.shop.dto.MemberSearchDto;
import com.shop.dto.MemberUpdateFormDto;
import com.shop.entity.AuthToken;
import com.shop.entity.Member;
import com.shop.entity.OAuth2Member;
import com.shop.mapstruct.MemberUpdateFormMapper;
import com.shop.repository.MemberRepository;
import com.shop.repository.OAuth2MemberRepository;
import com.shop.service.AuthTokenService;
import com.shop.service.EmailService;
import com.shop.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

/**
 * 회원 컨트롤러
 *
 * @author 공통
 * @version 1.0
 */
@Tag(name = "회원", description = "회원 관련 요청 처리")
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberUpdateFormMapper memberUpdateFormMapper;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final OAuth2MemberRepository oAuth2MemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthTokenService authTokenService;

    /**
     * 회원가입 페이지로 이동
     *
     * @param model 회원정보 필수 입력값을 담는 객체
     *
     * @return 회원가입 페이지로 반환
     */
    @Operation(summary = "회원가입 페이지", description = "회원가입 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 페이지 뷰")
    })
    @GetMapping(value = "/new")
    public String memberForm(Model model) {
        model.addAttribute("banks", Bank.values());
        model.addAttribute("memberFormDto", new MemberFormDto());

        return "member/memberForm";
    }

    @Operation(summary = "회원가입 처리", description = "회원가입 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 로그인 페이지 리다이렉션<br>실패: 회원가입 페이지 뷰")
    })
    @PostMapping(value = "/new")
    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model, HttpSession httpSession) {
        model.addAttribute("banks", Bank.values());

        if(!StringUtils.equals(memberFormDto.getCode(), httpSession.getAttribute("emailConfirmCode"))){
            FieldError fieldError = new FieldError("memberFormDto", "code", "인증코드가 같지 않습니다.");

            bindingResult.addError(fieldError);
        }

        if(bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);

            memberService.saveMember(member);
        } catch(IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());

            return "member/memberForm";
        }

        return "redirect:/members/login";
    }

    /**
     * 로그인 페이지로 이동
     *
     * @return memberLoginForm 로그인 페이지로 반환
     */
    @Operation(summary = "로그인 페이지", description = "로그인 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 페이지 뷰")
    })
    @GetMapping(value = "/login")
    public String loginMember() {
        return "/member/memberLoginForm";
    }


    /**
     * 로그인 에러 페이지로 이동
     *
     * @param model 로그인 에러메세지를 담는 객체
     *
     * @return memberLoginForm 로그인 페이지로 반환
     */
    @Operation(summary = "로그인 에러 페이지", description = "로그인 에러 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 페이지 뷰")
    })
    @GetMapping(value = "/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");

        return "/member/memberLoginForm";
    }

    @Operation(summary = "회원 정보 수정 페이지", description = "회원 정보 수정 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 페이지 뷰")
    })
    @GetMapping(value = "/update")
    public String updateMemberForm(Principal principal, Model model) {
        Member member = memberRepository.findByEmail(principal.getName());
        OAuth2Member oAuth2Member = oAuth2MemberRepository.findByMember(member);

        MemberUpdateFormDto memberUpdateFormDto = memberUpdateFormMapper.toDto(member);
        memberUpdateFormDto.setPassword("");

        model.addAttribute("banks", Bank.values());
        model.addAttribute("memberUpdateFormDto", memberUpdateFormDto);
        model.addAttribute("oAuth2Member", oAuth2Member);

        return "member/memberUpdateForm";
    }

    @Operation(summary = "회원 정보 수정 처리", description = "회원 정보 수정 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 처리 뷰")
    })
    @PostMapping(value = "/update")
    public String updateMember(@Valid MemberUpdateFormDto memberUpdateFormDto, BindingResult bindingResult, Principal principal, Model model) {
        Member member = memberRepository.findByEmail(principal.getName());
        OAuth2Member oAuth2Member = oAuth2MemberRepository.findByMember(member);

        model.addAttribute("banks", Bank.values());
        model.addAttribute("oAuth2Member", oAuth2Member);

        if(!memberUpdateFormDto.getPassword().equals("")) {
            String password = memberUpdateFormDto.getPassword();

            if(password.length() < 8 || password.length() > 16) {
                FieldError fieldError = new FieldError("memberUpdateFormDto", "password", "비밀번호는 8자 이상, 16자 이하로 입력해주세요.");
                bindingResult.addError(fieldError);
            }
        }

        if(bindingResult.hasErrors()) {
            return "member/memberUpdateForm";
        }

        try {
            member.setName(memberUpdateFormDto.getName());
            member.setAddress(memberUpdateFormDto.getAddress());
            member.setAddressDetail(memberUpdateFormDto.getAddressDetail());
            member.setPhone(memberUpdateFormDto.getPhone());
            member.setRefundBank(memberUpdateFormDto.getRefundBank());
            member.setRefundAccount(memberUpdateFormDto.getRefundAccount());
            member.setNoticeType(memberUpdateFormDto.getNoticeType());

            if(oAuth2Member == null && !memberUpdateFormDto.getPassword().equals("")) {
                member.setPassword(passwordEncoder.encode(memberUpdateFormDto.getPassword()));
            }

            memberRepository.save(member);
        } catch(IllegalStateException e) {
            log.error(e.getMessage(), e);

            model.addAttribute("errorMessage", e.getMessage());

            return "member/memberUpdateForm";
        }

        return "redirect:/";
    }

    /**
     * 비밀번호 찾기 페이지
     *
     * @return 비밀번호 찾기 뷰 경로
     */
    @Operation(summary = "비밀번호 찾기 페이지", description = "비밀번호 찾기 페이지 매핑")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 찾기 뷰"),
    })
    @GetMapping(value ="/findPassword")
    public String findPassword() {
        return "member/findPassword";
    }

    /**
     * 비밀번호 찾기 이메일 전송
     *
     * @param email 입력받은 이메일 정보
     * @param name 입력받은 이름 정보
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공 : 로그인 페이지 리다이렉트
     *         실패 : 비밀번호 찾기 뷰 경로
     */
    @Operation(summary = "비밀번호 찾기 이메일 전송", description = "비밀번호 찾기 이메일 전송 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 페이지 리다이렉션"),
    })
    @PostMapping(value ="/findPassword")
    public String findPasswordSendEmail(@RequestParam(name = "email") String email, @RequestParam(name = "name") String name, Model model) {
        try {
            memberService.checkEmailAndName(email, name);

            emailService.sendPasswordEmail(email);

            model.addAttribute("message", "이메일을 확인하여 비밀번호를 변경해주세요.");
            model.addAttribute("location","/members/login");
        } catch(Exception e) {
            log.error(e.getMessage(), e);

            model.addAttribute("errorMessage", e.getMessage());

            return "member/findPassword";
        }

        return "redirect";
    }

    /**
     * 비밀번호 변경 페이지
     *
     * @param code 인증코드 정보
     * @param email 이메일 정보
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 비밀번호 변경 뷰 경로
     */
    @Operation(summary = "비밀번호 변경 페이지", description = "비밀번호 변경 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 뷰"),
    })
    @GetMapping(value = "/updatePassword")
    public String updatePasswordForm(@RequestParam("code") String code, @RequestParam("email") String email, Model model) {
        AuthToken authtoken = authTokenService.getTokenByCode(code);

        if(!authTokenService.validateExpireToken(email, code)) {
            throw new IllegalStateException("만료된 토큰입니다.");
        }

        authTokenService.invalidateToken(email);

        model.addAttribute("memberId", authtoken.getMember().getId());

        return "member/updatePassword";
    }

    /**
     * 비밀번호 변경
     *
     * @param memberId 비밀번호 변경할 회원 아이디 정보
     * @param password 변경될 비밀번호 정보
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 성공 : 로그인 페이지 리다이렉트
     *         실패 : 비밀번호 변경 페이지 뷰 경로
     */
    @Operation(summary = "비밀번호 변경 페이지", description = "비밀번호 변경 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 뷰"),
    })
    @PostMapping(value = "/updatePassword")
    public String updatePassword(@RequestParam("memberId") Long memberId, @RequestParam("password") String password, Model model) {
        try {
            memberService.updatePassword(memberId, password);

            model.addAttribute("message", "비밀번호가 변경되었습니다.");
            model.addAttribute("location","/members/login");
        } catch(Exception e) {
            log.error(e.getMessage(), e);

            model.addAttribute("errorMessage", e.getMessage());

            return "member/updatePassword";
        }

        return "redirect";
    }

    /**
     * 회원가입 시 인증 이메일 전송
     *
     * @param email 이메일 정보
     * @param httpSession 식별하기 위한 세션 정보
     *
     * @return http 상태코드
     */
    @Operation(summary = "회원가입 인증 이메일 전송", description = "회원가입 인증 이메일 전송 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 인증 이메일 전송"),
            @ApiResponse(responseCode = "400", description = "회원가입 인증 이메일 전송 실패")
    })
    @PostMapping(value = "/signUpEmail")
    public @ResponseBody ResponseEntity sendEmailAuthCode(String email, HttpSession httpSession) {
        emailService.sendEmailAuthCode(email, httpSession);

        return new ResponseEntity<String>("", HttpStatus.OK);
    }

    /**
     * 관리자 회원 조회 페이지
     *
     * @param memberSearchDto 검색 필드 객체
     * @param page 페이징 번호
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 관리자 회원 조회 페이지 뷰 경로
     */
    @Operation(summary = "관리자 회원 조회 페이지", description = "관리자 회원 조회 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 회원 조회 페이지 뷰"),
    })
    @GetMapping(value = {"/admin/memberMng", "/admin/memberMng/{page}"})
    public String memberManage(MemberSearchDto memberSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent()? page.get() : 0, 10);
        Page<Member> members = memberService.getAdminMemberPage(memberSearchDto, pageable);

        model.addAttribute("members", members);
        model.addAttribute("memberSearchDto", memberSearchDto);
        model.addAttribute("maxPage", 5);

        return "member/memberMng";
    }

}
