package com.shop.controller;

import com.shop.constant.BidDepositType;
import com.shop.dto.BidDto;
import com.shop.dto.BidSearchDto;
import com.shop.dto.ReverseAuctionDto;
import com.shop.entity.Bid;
import com.shop.entity.Member;
import com.shop.repository.BidRepository;
import com.shop.repository.MemberRepository;
import com.shop.service.BidService;
import com.shop.service.ReverseAuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;

/**
 * 입찰 컨트롤러
 *
 * @author 유한종
 * @version 1.0
 */
@Tag(name = "입찰", description = "입찰 관련 요청 처리")
@Controller
@RequiredArgsConstructor
public class BidController {

    private final MemberRepository memberRepository;

    private final ReverseAuctionService reverseAuctionService;

    private final BidService bidService;

    private final BidRepository bidRepository;

    /**
     * 입찰 결제 페이지
     *
     * @param reverseAuctionId 역경매 ID
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 입찰 결제 페이지 뷰 경로
     */
    @Operation(summary = "입찰 결제 페이지", description = "입찰 결제 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "입찰 결제 페이지 뷰")
    })
    @GetMapping(value = "/bid/{reverseAuctionId}")
    public String bidPayment(@Parameter(description = "역경매 ID") @PathVariable("reverseAuctionId") Long reverseAuctionId,
                             Principal principal,
                             Model model) {
        Member member = memberRepository.findByEmail(principal.getName());
        ReverseAuctionDto reverseAuctionDto = reverseAuctionService.getReverseAuctionDtl(reverseAuctionId);

        model.addAttribute("member", member);
        model.addAttribute("reverseAuctionDto", reverseAuctionDto);

        return "bid/bidPayment";
    }

    /**
     * 입찰 완료 페이지
     *
     * @param reverseAuctionId 역경매 ID
     * @param paramMap 사용자 전달 파라메터 맵 객체
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 입찰 완료 페이지 뷰 경로
     */
    @Operation(summary = "입찰 완료 페이지", description = "입찰 완료 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "입찰 완료 페이지 뷰")
    })
    @PostMapping(value = "/bid/{reverseAuctionId}")
    public String bidPaymentProcess(@Parameter(description = "역경매 ID") @PathVariable("reverseAuctionId") Long reverseAuctionId,
                                    @Parameter(description = "사용자 전달 파라메터 맵 객체", hidden = true) @RequestParam Map<String, String> paramMap,
                                    Principal principal,
                                    Model model) {
        BidDepositType bidDepositType = BidDepositType.valueOf(paramMap.get("bidDepositType").toUpperCase());

        Member member = memberRepository.findByEmail(principal.getName());

        Bid bid;

        if(bidDepositType.equals(BidDepositType.KAKAO_PAY)) {
            Long payId = Long.parseLong(paramMap.get("tid").substring(1));

            bid = bidService.saveBidWithPayment(member.getEmail(), reverseAuctionId, payId);
        } else {
            bid = bidService.saveBid(member.getEmail(), reverseAuctionId);
        }

        model.addAttribute("bid", bid);

        return "bid/bidComplete";
    }

    /**
     * 입찰 내역 페이지
     *
     * @param bidSearchDto 검색 필드 정보
     * @param page 페이징 번호
     * @param principal 사용자 인증 정보 객체
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 입찰 내역 페이지 뷰 경로
     */
    @Operation(summary = "입찰 내역 페이지", description = "입찰 내역 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "입찰 내역 페이지 뷰")
    })
    @GetMapping(value = {"/bids","/bids/{page}"})
    public String bidList(@Parameter(description = "검색 필드 정보") BidSearchDto bidSearchDto,
                          @Parameter(description = "페이징 번호") @PathVariable("page") Optional<Integer> page,
                          Principal principal,
                          Model model) {
        Member member = memberRepository.findByEmail(principal.getName());

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);
        Page<BidDto> bidDtoList = bidService.getUserBidPage(principal.getName(), bidSearchDto, pageable);

        model.addAttribute("bidDtoList", bidDtoList);
        model.addAttribute("bidSearchDto", bidSearchDto);
        model.addAttribute("maxPage", 5);
        model.addAttribute("depositName", bidService.getUniqueDepositName(member));

        return "bid/bidList";
    }

    /**
     * 관리자 입찰 내역 페이지
     *
     * @param bidSearchDto 검색 필드 정보
     * @param page 페이징 번호
     * @param model 뷰에 전달할 모델 객체
     *
     * @return 관리자 입찰 내역 페이지 뷰 경로
     */
    @Operation(summary = "관리자 입찰 내역 페이지", description = "관리자 입찰 내역 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 입찰 내역 페이지 뷰")
    })
    @GetMapping(value = {"/admin/bids","/admin/bids/{page}"})
    public String bidMng(@Parameter(description = "검색 필드 정보") BidSearchDto bidSearchDto,
                         @Parameter(description = "페이징 번호") @PathVariable("page") Optional<Integer> page,
                         Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 10);
        Page<BidDto> bidDtoList = bidService.getAdminBidPage(bidSearchDto, pageable);

        model.addAttribute("bidDtoList", bidDtoList);
        model.addAttribute("bidSearchDto", bidSearchDto);
        model.addAttribute("maxPage", 10);

        return "bid/bidMng";
    }

    /**
     * 관리자 입찰 결제 확인 처리
     *
     * @param bidId 입찰 ID
     *
     * @return 관리자 입찰 내역 리다이렉션
     */
    @Operation(summary = "관리자 입찰 결제 확인 처리", description = "관리자 입찰 결제 확인 처리 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 입찰 내역 리다이렉션")
    })
    @GetMapping(value = "/admin/bid/{bidId}/approve")
    public String bidApprove(@Parameter(description = "입찰 ID") @PathVariable("bidId") Long bidId) {
        bidService.approveBid(bidId);

        return "redirect:/admin/bids";
    }

    /**
     * 관리자 입찰 환불 정보 조회
     *
     * @param bidId 입찰 ID
     *
     * @return 관리자 입찰 환불 정보
     */
    @Operation(summary = "관리자 입찰 환불 정보 조회", description = "관리자 입찰 환불 정보 조회 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 입찰 환불 정보")
    })
    @GetMapping(value = "/admin/bid/{bidId}/refund")
    public @ResponseBody ResponseEntity bidRefund(@Parameter(description = "입찰 ID") @PathVariable("bidId") Long bidId) {
        try {
            Bid bid = bidRepository.findById(bidId).orElseThrow(EntityNotFoundException::new);
            Member member = bid.getMember();

            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            String result = "계좌 번호 : " + "(" + member.getRefundBank() + ") " + member.getRefundAccount()
                    + "\n입금자명 : " + bid.getDepositName()
                    + "\n환불 금액 : ₩" + decimalFormat.format(bid.getDepositAmount());

            return new ResponseEntity<String>(result, HttpStatus.OK);
        } catch(Exception e) {
            return new ResponseEntity<String>("환불 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }

}
