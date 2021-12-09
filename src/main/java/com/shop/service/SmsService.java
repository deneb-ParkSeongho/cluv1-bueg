package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.entity.Item;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.SmsNotice;
import com.shop.repository.ItemRepository;
import com.shop.repository.OrderRepository;
import com.shop.repository.SmsNoticeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;

/**
 * SMS 알림 서비스
 *
 * @author 조우진
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final SmsNoticeRepository smsNoticeRepository;

    @Value("${spring.sms.api-key}")
    private String smsApiKey;

    @Value("${spring.sms.api-secret}")
    private String smsApiSecret;

    /**
     * SMS 전송 메소드
     *
     * @param phone 회원 휴대폰 번호
     * @param text SMS 내용
     *
     * @return SMS 전송
     */
    @Async
    public void sendSms(String phone, String text) {
        Message coolsms = new Message(smsApiKey, smsApiSecret);
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("to", phone);
        params.put("from", "010-3583-7031");
        params.put("type", "SMS");
        params.put("text", text);
        params.put("app_version", "test app 1.2");

        try {
            coolsms.send(params);
        } catch (CoolsmsException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 주문 알림 SMS 전송 메소드
     *
     * @param phone 회원 휴대폰 번호
     * @param orderDto 주문 정보가 들어있는 객체
     *
     * @return SMS 전송, SMS 전송량 카운트
     */
    public void sendOrderSms(String phone, OrderDto orderDto) {
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

        StringBuffer sb = new StringBuffer("[Bueg] 주문 상품 내역\n");
        sb.append("주문 상품 : ");
        sb.append(item.getItemNm());
        sb.append("\n주문 수량 : ");
        sb.append(orderDto.getCount());
        sb.append("\n주문 금액 : ");
        sb.append(item.getPrice() * orderDto.getCount());
        sb.append("원");

        this.sendSms(phone, sb.toString());
        this.addSmsCount();
    }

    /**
     * 장바구니 주문 알림 SMS 전송 메소드
     *
     * @param phone 회원 휴대폰 번호
     * @param orderDtoList 장바구니 주문 정보가 들어있는 리스트 객체
     * @param totalPrice 장바구니 주문 총 가격
     *
     * @return SMS 전송, SMS 전송량 카운트
     */
    public void sendCartOrderSms(String phone, List<OrderDto> orderDtoList, Integer totalPrice) {
        StringBuffer sb = new StringBuffer("[Bueg]주문상품 내역\n");

        for(OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

            sb.append(item.getItemNm());
            sb.append("(");
            sb.append(item.getPrice());
            sb.append(" 원) x ");
            sb.append(orderDto.getCount());
            sb.append("개\n");
        }

        sb.append("\n주문 금액 : ");
        sb.append(totalPrice);
        sb.append("원\n");

        this.sendSms(phone, sb.toString());
        this.addSmsCount();
    }

    /**
     * SMS 알림 전송량 카운트 메소드
     *
     * @return SMS 알림 전송량 카운트 저장
     */
    public void addSmsCount() {
        SmsNotice smsNotice = new SmsNotice();

        smsNoticeRepository.save(smsNotice);
    }

}
