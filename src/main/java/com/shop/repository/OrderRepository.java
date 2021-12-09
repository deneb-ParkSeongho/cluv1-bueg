package com.shop.repository;

import com.shop.constant.GiftStatus;
import com.shop.entity.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 주문 레포지토리
 *
 * @author Lychee
 * @version 1.0
 */

@Tag(name = "주문 레포지토리", description = "주문 레포지토리 목록")
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o where o.member.email = :email order by o.orderDate desc")
    List<Order> findOrders(@Param("email") String email, Pageable pageable);

    /**
     * 선물 리스트 조회 쿼리
     *
     * @param email 해당 회원의 이메일
     * @param giftStatus 구매/선물 상태
     * @param pageable 페이징 처리에 필요한 정보
     *
     * @return null
     */
    @Query("select o from Order o where o.member.email = :email and o.giftStatus = :giftStatus order by o.orderDate desc")
    List<Order> findOrdersStatus(@Param("email") String email, Pageable pageable, @Param("giftStatus") GiftStatus giftStatus);

    @Query("select o from Order o where 1=1 AND o.orderStatus = 'RETURN' order by o.orderDate desc")
    List<Order> findOrdersForReturnList(@Param("email") String email, Pageable pageable);

    @Query("select count(o) from Order o where o.member.email = :email")
    Long countOrder(@Param("email") String email);

    @Query("select count(o) from Order o where 1=1 AND o.orderStatus = 'RETURN'")
    Long countOrderForReturnList(@Param("email") String email);

}
