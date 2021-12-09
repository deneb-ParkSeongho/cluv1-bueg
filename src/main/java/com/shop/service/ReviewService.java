package com.shop.service;

import com.shop.dto.ReviewFormDto;
import com.shop.dto.ReviewImgDto;
import com.shop.dto.ReviewItemDto;
import com.shop.entity.OrderItem;
import com.shop.entity.ReviewImg;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.ReviewImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 서비스
 * @author 강은별
 * @version 1.0
 */

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final OrderItemRepository orderItemRepository;
    private final ReviewImgService reviewImgService;
    private final ReviewImgRepository reviewImgRepository;

    /**
     *
     * @param orderItemId 주문번호가 담겨있는 파라미터
     * @param reviewFormDto 상품 리뷰 정보를 담을 객체
     * @param reviewImgFile 상품 리뷰 이미지 정보를 담을 객체
     * @return 리뷰를 작성한 주문번호 반환
     * @throws Exception 이미지 업로드를 하는 중 Multipartfile 인자에  null이 입력되는 경우
     */
    public Long saveReview(Long orderItemId, ReviewFormDto reviewFormDto, List<MultipartFile> reviewImgFile) throws Exception {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(EntityNotFoundException::new);
        orderItem.createReview(reviewFormDto);
        orderItem.setReviewYn("Y");

        for(int i = 0; i < reviewImgFile.size(); i++) {
            ReviewImg reviewImg = new ReviewImg();
            reviewImg.setOrderItem(orderItem);

            reviewImgService.saveReviewImg(reviewImg, reviewImgFile.get(i));
        }

        return orderItem.getId();
    }

    /**
     *
     * @param orderItemId 상품 리뷰를 불러오기 위한 주문 번호
     * @return 리뷰 정보가 담겨있는 객체 반환
     */
    @Transactional(readOnly = true)
    public ReviewFormDto getReviewDtl(Long orderItemId) {
        List<ReviewImg> reviewImgList = reviewImgRepository.findByOrderItemIdOrderByIdAsc(orderItemId);
        List<ReviewImgDto> reviewImgDtoList = new ArrayList<>();

        for(ReviewImg reviewImg : reviewImgList){
            ReviewImgDto reviewImgDto = ReviewImgDto.of(reviewImg);
            reviewImgDtoList.add(reviewImgDto);
        }

        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(EntityNotFoundException::new);

        ReviewFormDto reviewFormDto = ReviewFormDto.of(orderItem);
        reviewFormDto.setReviewImgDtoList(reviewImgDtoList);

        return reviewFormDto;
    }

    /**
     *
     * @param orderItemId 수정할 리뷰의 주문번호를 담고있는 파라미터
     * @param reviewFormDto 수정할 리뷰 정보를 담고있는 객체
     * @param reviewImgFile 수정할 리뷰 이미지 정보를 담고있는 객체
     * @return 수정할 리뷰의 주문번호를 반환
     * @throws Exception 이미지 업로드를 하는 중 Multipartfile 인자에  null이 입력되는 경우
     */
    public Long updateReview(Long orderItemId, ReviewFormDto reviewFormDto, List<MultipartFile> reviewImgFile) throws Exception {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(EntityNotFoundException::new);
        orderItem.updateReview(reviewFormDto);

        List<Long> reviewImgIds = reviewFormDto.getReviewImgIds();

        for(int i = 0; i < reviewImgFile.size(); i++){
            reviewImgService.updateReviewImg(reviewImgIds.get(i), reviewImgFile.get(i));
        }

        return orderItem.getId();

    }

    /**
     *
     * @param orderItemId 삭제할 리뷰의 주문번호를 담고있는 파라미터
     * @param reviewFormDto 삭제할 리뷰 정보를 담고있는 객체
     * @return 삭제할 리뷰의 주문번호를 반환
     */
    public Long deleteReview(Long orderItemId, ReviewFormDto reviewFormDto) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(EntityNotFoundException::new);
        orderItem.deleteReview(reviewFormDto);
        orderItem.setReviewYn("N");

        return orderItem.getId();
    }

    /**
     *
     * @param itemId 제품 별로 상세내역에 리뷰를 보여주기 위한 제품 번호
     * @return 제품의 리뷰 정보가 담겨있는 list 반환
     */
    @Transactional(readOnly = true)
    public List<ReviewItemDto> getReviewItem(Long itemId) {
        List<OrderItem> orderItems = orderItemRepository.findByItemIdAndReviewYn(itemId, "Y");
        List<ReviewItemDto> reviewItemDtoList = new ArrayList<>();

        for(OrderItem orderItem : orderItems){
            ReviewItemDto reviewItemDto = new ReviewItemDto(orderItem);
            reviewItemDto.addReviewItemDto(reviewItemDto);

            reviewItemDtoList.add(reviewItemDto);
        }

        return reviewItemDtoList;
    }

    /**
     *
     * @param itemId 제품 별로 상세내역에 리뷰 이미지를 보여주기 위한 제품 번호
     * @return 제품의 리뷰 이미지 정보가 담겨있는 list 반환
     */
    @Transactional(readOnly = true)
    public List<ReviewImgDto> getReviewItemImg(Long itemId) {
        List<OrderItem> orderItems = orderItemRepository.findByItemIdAndReviewYn(itemId, "Y");
        List<ReviewImgDto> reviewImgDtoList = new ArrayList<>();

        for(OrderItem orderItem : orderItems){
            Long orderItemId = orderItem.getId();

            ReviewImg reviewImg = reviewImgRepository.findByOrderItemId(orderItemId);

            ReviewImgDto reviewImgDto = new ReviewImgDto();
            reviewImgDto.setId(reviewImg.getId());
            reviewImgDto.setReviewImgName(reviewImg.getReviewImgName());
            reviewImgDto.setReviewImgUrl(reviewImg.getReviewImgUrl());
            reviewImgDto.setReviewOriImgName(reviewImg.getReviewOriImgName());

            reviewImgDtoList.add(reviewImgDto);
        }

        return reviewImgDtoList;
    }

}
