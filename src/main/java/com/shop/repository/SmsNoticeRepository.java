package com.shop.repository;

import com.shop.entity.SmsNotice;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SMS 알림 레포지토리
 *
 * @author 조우진
 * @version 1.0
 */
public interface SmsNoticeRepository extends JpaRepository<SmsNotice, Long> {

}
