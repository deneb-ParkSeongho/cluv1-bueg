package com.shop.repository;

import com.shop.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 카테고리 레포지토리
 *
 * @author Lychee
 * @version 1.0
 */

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 해당 카테고리 조회 메소드
     * @param cateCode 카테고리 코드
     * @return null
     */
    Category findByCateCode(Long cateCode);

    /**
     * 카테고리 목록 전체 조회 메소드
     * @return null
     */
    List<Category> findAllByOrderByCateCodeAsc();

}
