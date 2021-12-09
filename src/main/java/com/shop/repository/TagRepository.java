package com.shop.repository;


import com.shop.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
/**
 * 태그 레포지토리
 *
 * @author Eloy
 * @version 1.0
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

     /**
      * 태그를 누적주문량의 내림차순으로 가져오는 쿼리 메소드
      */
     @Query("select t from Tag t order by t.totalSell desc")
     List<Tag> findAllByOrderByTotalSellDesc();

}
