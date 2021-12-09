package com.shop.service;

import com.shop.entity.ReviewImg;
import com.shop.repository.ReviewImgRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;

/**
 * 리뷰 이미지 서비스
 * @author 강은별
 * @version 1.0
 */

@Service
@Transactional
@RequiredArgsConstructor
@Tag(name = "리뷰 이미지 서비스", description = "리뷰 이미지 서비스 목록")
public class ReviewImgService {

    @Value("${reviewImgLocation}")
    private String reviewImgLocation;

    private final ReviewImgRepository reviewImgRepository;

    private final FileService fileService;

    /**
     *
     * @param reviewImg 리뷰 작성 시 리뷰 이미지를 저장할 객체
     * @param reviewImgFile 이미지 업로드를 위한 객체
     * @throws Exception 이미지 업로드를 하는 중 Multipartfile 인자에  null이 입력되는 경우
     */
    public void saveReviewImg(ReviewImg reviewImg, MultipartFile reviewImgFile) throws Exception {
        String reviewOriImgName = reviewImgFile.getOriginalFilename();
        String reviewImgName = "";
        String reviewImgUrl = "";

        if(!StringUtils.isEmpty(reviewOriImgName)){
            reviewImgName = fileService.uploadFile(reviewImgLocation, reviewOriImgName, reviewImgFile.getBytes());
            reviewImgUrl = "/images/review/" + reviewImgName;
        }

        reviewImg.updateReviewImg(reviewOriImgName, reviewImgName, reviewImgUrl);

        reviewImgRepository.save(reviewImg);
    }

    /**
     *
     * @param reviewImgId 수정 폼에 불러올 이미지를 위한 리뷰 이미지 Id
     * @param reviewImgFile 리뷰 이미지 수정을 위한 객체
     * @throws Exception 이미지 업로드를 하는 중 Multipartfile 인자에  null이 입력되는 경우
     */
    public void updateReviewImg(Long reviewImgId, MultipartFile reviewImgFile) throws Exception {
        if(!reviewImgFile.isEmpty()){
            ReviewImg savedReviewImg = reviewImgRepository.findById(reviewImgId).orElseThrow(EntityNotFoundException::new);

            if(!StringUtils.isEmpty(savedReviewImg.getReviewImgName())){
                fileService.deleteFile(reviewImgLocation + "/" + savedReviewImg.getReviewImgName());
            }

            String reviewOriImgName = reviewImgFile.getOriginalFilename();
            String reviewImgName = fileService.uploadFile(reviewImgLocation, reviewOriImgName, reviewImgFile.getBytes());
            String reviewImgUrl = "/images/review/" + reviewImgName;

            savedReviewImg.updateReviewImg(reviewOriImgName, reviewImgName, reviewImgUrl);
        }
    }

    /**
     *
     * @param orderItemId 리뷰 삭제 시 리뷰를 작성한 제품의 주문 번호가 담긴 파라미터
     */
    public void deleteReviewImg(Long orderItemId) {
        ReviewImg reviewImg = reviewImgRepository.findByOrderItemId(orderItemId);

        reviewImgRepository.delete(reviewImg);
    }

}
