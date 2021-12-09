package com.shop.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

/**
 * 해시 함수 서비스
 *
 * @author 조우진
 * @version 1.0
 */
@Service
public class EncryptionService {

    /**
     * MD5 해시 처리 메소드
     *
     * @param content 해시 처리할 내용
     *
     * @return MD5로 해시 처리된 문자열
     */
    public String encryptMD5(String content) {
        return this.encrypt(content, "MD5");
    }

    /**
     * 해시 처리 메소드
     *
     * @param content 해시 처리할 내용
     * @param algorithm 알고리즘 종류
     *
     * @return 성공 : 해시 처리된 문자열
     *         실패 : null
     */
    public String encrypt(String content, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.reset();

            byte[] bytes = content.getBytes();
            byte[] digest = md.digest(bytes);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch(Exception e) {
            return null;
        }
    }

}
