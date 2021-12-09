package com.shop.service;

import com.shop.entity.AuthToken;
import com.shop.entity.Member;
import com.shop.repository.AuthTokenRepository;
import com.shop.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증토큰 서비스
 *
 * @author 조우진
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;
    private final MemberRepository memberRepository;
    private final EncryptionService encryptionService;

    /**
     * 인증코드 가져오기 메소드
     *
     * @param email 이메일 인증에 입력받은 이메일
     *
     * @return 해시처리된 코드
     */
    public String getTokenCode(String email) {
        return encryptionService.encryptMD5(email + LocalDateTime.now());
    }

    /**
     * 이메일로 토큰 가져오기 메소드
     *
     * @param email 회원 이메일
     *
     * @return 회원의 가장 최신의 토큰
     */
    public AuthToken getTokenByEmail(String email) {
        Member member = memberRepository.findByEmail(email);

        return authTokenRepository.findFirstByMemberOrderByRegTimeDesc(member);
    }

    /**
     * 코드로 토큰 가져오기 메소드
     *
     * @param code 인증코드
     *
     * @return 회원의 가장 최신의 토큰
     */
    public AuthToken getTokenByCode(String code){
        return authTokenRepository.findFirstByCodeOrderByRegTimeDesc(code);
    }

    /**
     * 토큰 생성 메소드
     *
     * @param email 회원 이메일
     *
     * @return 토큰 생성하여 저장
     */
    public AuthToken createToken(String email) {
        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new IllegalStateException("등록된 이메일이 아닙니다.");
        }

        String tokenCode = this.getTokenCode(email);
        LocalDateTime tokenExpireDate = LocalDateTime.now().plusHours(1);

        AuthToken authToken = new AuthToken();
        authToken.setMember(member);
        authToken.setCode(tokenCode);
        authToken.setExpireDate(tokenExpireDate);

        return authTokenRepository.save(authToken);
    }

    /**
     * 토큰 만료 처리 메소드
     *
     * @param email 회원 이메일
     *
     * @return 토큰 사용 상태 변경하여 저장
     */
    public void invalidateToken(String email) {
        AuthToken authToken = this.getTokenByEmail(email);
        authToken.setUseYn("Y");

        authTokenRepository.save(authToken);
    }

    /**
     * 토큰 만료 확인 메소드
     *
     * @param email 회원 이메일
     * @param code 인증코드
     *
     * @return 토큰이 만료된 경우 true, 만료되지 않은 경우 false
     */
    public boolean validateExpireToken(String email, String code) {
        AuthToken authToken = this.getTokenByEmail(email);

        if(!code.equals(authToken.getCode())
        || authToken.getUseYn().equals("Y")
        || authToken.getExpireDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

}
