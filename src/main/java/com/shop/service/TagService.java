package com.shop.service;

import com.shop.entity.Tag;
import com.shop.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 태그 서비스
 *
 * @author Eloy
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 태그 목록 호출
     *
     * @return tagRepository.findAllByOrderByTotalSellDesc()<br> 태그목록을 불러오는 메소드 호출
     */
    @Transactional(readOnly = true)
    public List<Tag> getTagList() {
        return tagRepository.findAllByOrderByTotalSellDesc();
    }

}
