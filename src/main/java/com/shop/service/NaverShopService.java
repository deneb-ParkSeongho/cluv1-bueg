package com.shop.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.NaverShopItemDto;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 네이버 쇼핑 API 서비스
 *
 * @author Kuhn
 * @version 1.0
 */
@Slf4j
@Service
public class NaverShopService {

    @Value("${shop.naver.client.id}")
    private String clientId;

    @Value("${shop.naver.client.secret}")
    private String clientSecret;

    /**
     * 장바구니 상품 추가 메소드
     *
     * @param name 검색할 상품명을 담은 객체
     *
     * @return naverShopItemDtoList 네이버 쇼핑 API에서 반환한 값 중 상품명, 최저가 , 상품구매링크 담아 리스트 반환
     */

    public List<NaverShopItemDto> search(String name) {
        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", clientId);
        headers.add("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);

        ResponseEntity<String> responseEntity = rest.exchange("https://openapi.naver.com/v1/search/shop.json?query=" + name, HttpMethod.GET, requestEntity, String.class);
        String responseBody = responseEntity.getBody();

        List<NaverShopItemDto> naverShopItemDtoList = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> responseObj = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) responseObj.get("items");

            for(Map<String, Object> item : items) {
                NaverShopItemDto naverShopItemDto = new NaverShopItemDto();
                naverShopItemDto.setTitle((String) item.get("title"));
                naverShopItemDto.setLprice(Integer.parseInt((String) item.get("lprice")));
                naverShopItemDto.setSlink((String) item.get("link"));

                naverShopItemDtoList.add(naverShopItemDto);
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

        return naverShopItemDtoList;
    }


}


