package com.shop.service;

import com.shop.dto.MemberSearchDto;
import com.shop.entity.Member;
import com.shop.entity.OAuth2Member;
import com.shop.repository.MemberRepository;
import com.shop.repository.OAuth2MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스
 *
 * @author 공통
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final OAuth2MemberRepository oAuth2MemberRepository;

    /**
     *  신규 회원을 저장
     *
     * @param member 회원의 정보를 담고 있는 객체
     *
     * @return  memberRepository.save(member) 회원을 저장하는 메소드 반환
     */
    public Member saveMember(Member member) {
        validateDuplicateMember(member);

        return memberRepository.save(member);
    }

    /**
     *  기존 회원 여부를 판단
     *
     * @param member 회원의 정보를 담고 있는 객체
     *
     * @throws IllegalStateException 소셜계정회원과 일반기존회원에게 알림 문구 전송
     */
    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());

        if(findMember != null) {
            OAuth2Member findSocialMember = oAuth2MemberRepository.findByMember(findMember);

            if(findSocialMember != null) {
                throw new IllegalStateException("해당 이메일은 소셜 로그인으로 등록된 이메일입니다, 소셜 로그인을 이용해 주세요.");
            }

            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    /**
     *  로그인한 회원 이름 호출
     *
     * @auther oLO
     * @param email 현재 로그인한 계정의 이메일
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    public int getPointByEmail(String email) {
        Member member = memberRepository.findByEmail(email);

        return member.getPoint();
    }

    public void updatePassword(Long memberId, String password) {
        memberRepository.updatePassword(memberId, new BCryptPasswordEncoder().encode(password));
    }

    public boolean checkEmailAndName(String email, String name) {
        Member member = memberRepository.findByEmail(email);

        if(member == null || !member.getName().equals(name)) {
            throw new IllegalStateException("이메일과 이름이 일치하지 않습니다.");
        }

        return true;
    }

    @Transactional(readOnly = true)
    public Page<Member> getAdminMemberPage(MemberSearchDto memberSearchDto, Pageable pageable){
        return memberRepository.getAdminMemberPage(memberSearchDto, pageable);
    }

}
