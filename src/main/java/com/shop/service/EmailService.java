package com.shop.service;

import com.shop.dto.MemberFormDto;
import com.shop.dto.OrderDto;
import com.shop.entity.EmailNotice;
import com.shop.entity.Item;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.repository.AuthTokenRepository;
import com.shop.repository.EmailNoticeRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.OrderRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 이메일 알림 서비스
 *
 * @author 조우진
 * @version 1.0
 */
@Async
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    private final AuthTokenService authTokenService;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final EmailNoticeRepository emailNoticeRepository;

    /**
     * 이메일 전송 메소드
     *
     * @param to 이메일 수신자
     * @param subject 이메일 제목
     * @param text 이메일 내용
     *
     * @return 이메일 전송
     */
    public void sendEmail(String to, String subject, String text) {
        this.sendEmail(to, subject, text, false);
    }

    /**
     * 이메일 전송 메소드(파라미터 값 지정)
     *
     * @param to 이메일 수신자
     * @param subject 이메일 제목
     * @param text 이메일 내용
     * @param html html 여부
     *
     * @return 이메일 전송
     */
    public void sendEmail(String to, String subject, String text, boolean html) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text, html);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 인증코드 이메일 전송 메소드
     *
     * @param email 수신 이메일
     * @param httpSession 식별하기 위한 세션 정보
     *
     * @return 인증코드 포함된 이메일 전송
     */
    public void sendEmailAuthCode(String email, HttpSession httpSession) {
        String code = authTokenService.getTokenCode(email).substring(0, 6);
        String subject = "[Bueg] 인증코드입니다.";
        String text = "이메일 인증코드는 " + code + "입니다.";

        httpSession.setAttribute("emailConfirmCode", code);

        this.sendEmail(email, subject, text);
    }

    /**
     * 주문 알림 이메일 전송 메소드
     *
     * @param email 회원 이메일
     * @param orderDto 주문 정보가 들어있는 객체
     *
     * @return 이메일 전송, 이메일 카운트
     */
    public void sendOrderEmail(String email, OrderDto orderDto) {
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

        String subject = "주문 상품 내역입니다.";

        StringBuffer sb = new StringBuffer();
        sb.append("[Bueg] 주문 상품 내역입니다.\n");
        sb.append("주문 상품 : ");
        sb.append(item.getItemNm());
        sb.append("\n주문 수량 : ");
        sb.append(orderDto.getCount());
        sb.append("\n주문 금액 : ");
        sb.append(item.getPrice() * orderDto.getCount());
        sb.append("원 입니다.\n");

        this.sendEmail(email, subject, sb.toString());
        this.addEmailCount();
    }

    /**
     * 장바구니 주문 알림 이메일 전송 메소드
     *
     * @param email 회원 이메일
     * @param orderDtoList 장바구니 주문 정보가 들어있는 리스트 객체
     * @param totalPrice 장바구니 주문 총 가격
     *
     * @return 이메일 전송, 이메일 카운트
     */
    public void sendCartOrderEmail(String email, List<OrderDto> orderDtoList, Integer totalPrice) {
        String subject = "주문 상품 내역입니다.";

        StringBuffer sb = new StringBuffer("[Bueg] 주문 상품 내역입니다.\n\n");

        for(OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

            sb.append(item.getItemNm());
            sb.append("(");
            sb.append(item.getPrice());
            sb.append(" 원) x ");
            sb.append(orderDto.getCount() + "개\n");
        }

        sb.append("\n주문 금액 : ");
        sb.append(totalPrice);
        sb.append("원\n");

        this.sendEmail(email, subject, sb.toString());
        this.addEmailCount();
    }

    /**
     * 비밀번호 변경 URL 이메일 전송 메소드
     *
     * @param email 회원 이메일
     *
     * @return 비밀번호 변경 URL 포함된 이메일 전송
     */
    public void sendPasswordEmail(String email) {
        String subject = "[Bueg] 비밀번호를 변경해주세요";

        StringBuffer sb = new StringBuffer();
        sb.append("<a href='http://localhost:8080/members/updatePassword?code=");
        sb.append(authTokenService.createToken(email).getCode());
        sb.append("&email=");
        sb.append(email);
        sb.append("'>비밀번호 변경페이지</a>");

        this.sendEmail(email, subject, sb.toString(), true);
    }

    /**
     * 이메일 알림 전송량 카운트 메소드
     *
     * @return 이메일 알림 전송량 카운트 저장
     */
    public void addEmailCount() {
        EmailNotice emailNotice = new EmailNotice();

        emailNoticeRepository.save(emailNotice);
    }

}