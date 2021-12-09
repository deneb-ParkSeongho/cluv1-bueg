package com.shop.repository;

import com.shop.constant.GiftStatus;
import com.shop.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 주문 레포지토리
 *
 * @author 문예은
 * @version 1.0
 */


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o where o.member.email = :email order by o.orderDate desc")
    List<Order> findOrders(@Param("email") String email, Pageable pageable);

    @Query("select o from Order o where o.member.email = :email and o.giftStatus = :giftStatus order by o.orderDate desc")
    List<Order> findOrdersStatus(@Param("email") String email, Pageable pageable, @Param("giftStatus") GiftStatus giftStatus);

    /**
     *
     *
     * @param email 로그인한 회원의 반품 데이터를 페이징 조건에 맞춰 조회
     * @param pageable 페이징 조건
     *
     *
     */

    @Query("select o from Order o where 1=1 AND o.orderStatus = 'RETURN' order by o.orderDate desc")
    List<Order> findOrdersForReturnList(@Param("email") String email, Pageable pageable);

    @Query("select count(o) from Order o where o.member.email = :email")
    Long countOrder(@Param("email") String email);

    /**
     *
     *
     * @param email 로그인한 회원의 반품 개수가 몇 개인지 조회
     *
     *
     */

    @Query("select count(o) from Order o where 1=1 AND o.orderStatus = 'RETURN'")
    Long countOrderForReturnList(@Param("email") String email);

}
