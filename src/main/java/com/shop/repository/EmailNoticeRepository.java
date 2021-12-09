package com.shop.repository;

import com.shop.entity.EmailNotice;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이메일 알림 레포지토리
 *
 * @author 조우진
 * @version 1.0
 */
public interface EmailNoticeRepository extends JpaRepository<EmailNotice, Long> {

}
