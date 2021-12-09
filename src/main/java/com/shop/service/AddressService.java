package com.shop.service;

import com.shop.dto.AddressDto;
import com.shop.entity.Address;
import com.shop.entity.Member;
import com.shop.repository.AddressRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 주소 서비스
 *
 * @author Lychee
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    /**
     * 주소록 저장 메소드
     *
     * @param addressDto 주소록 정보를 담은 객체
     * @param email 해당 회원의 이메일
     *
     * @return 주소록에 저장되는 회원 아이디
     */
    public Long saveAddress(AddressDto addressDto, String email) {
        Member member = memberRepository.findByEmail(email);
        Address address = Address.createAddress(member, addressDto);

        addressRepository.save(address);

        return address.getId();
    }

}
