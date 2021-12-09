package com.shop.repository;

import com.shop.entity.AuthToken;
import com.shop.entity.Member;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 인증토큰 레포지토리
 *
 * @author 조우진
 * @version 1.0
 */
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    /**
     * 회원의 가장 최신의 토큰 가져오는 메소드
     * @param member 회원정보
     */
    AuthToken findFirstByMemberOrderByRegTimeDesc(Member member);
    /**
     * 인증코드로 회원의 가장 최신의 토큰 가져오는 메소드
     * @param code 인증코드
     */
    AuthToken findFirstByCodeOrderByRegTimeDesc(String code);

}
