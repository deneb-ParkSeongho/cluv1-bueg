package com.shop.service;

import com.shop.entity.Category;
import com.shop.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 서비스
 *
 * @author Lychee
 * @version 1.0
 */

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 마스터 데이터 조회 메소드
     * @return categoryRepository.findAllByOrderByCateCodeAsc() 카테고리 목록 조회 값 반환
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoryList() {
        return categoryRepository.findAllByOrderByCateCodeAsc();
    }

}
