package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.repository.MemberRepository;
import com.shop.service.CartService;
import com.shop.service.EmailService;
import com.shop.service.MemberService;
import com.shop.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * 장바구니 컨트롤러
 *
 * @author 공통
 * @version 1.0
 */
@Tag(name = "장바구니", description = "장바구니 관련 요청 처리")
@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 상품 추가 메소드
     *
     * @param cartItemDto 장바구니에 담을 상품 정보를 담은 객체
     * @param bindingResult 바인딩의 에러 결과
     * @param principal 현재 로그인한 회원
     *
     * @return cartItemId 생성된 장바구니<br>
     *  HttpStatus.OK 요청이 성공했다는 http 응답 상태코드 반환
     */
    @Operation(summary = "장바구니 상품 추가 메소드", description = "장바구니 상품 추가")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 상품 추가"),
            @ApiResponse(responseCode = "400", description = "장바구니 상품 추가 실패")
    })
    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal) {
        if(bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();

            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for(FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();

        Long cartItemId;

        try {
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    /**
     * 장바구니 페이지
     *
     * @param model 상품 상세정보를 담을 객체
     * @param principal 현재 로그인한 회원
     *
     * @return 장바구니 목록 페이지을 반환
     */
    @Operation(summary = "장바구니 페이지", description = "장바구니 페이지 매핑 메소드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 페이지 뷰 경로"),
    })
    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model) {
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());

        model.addAttribute("cartItems", cartDetailList);

        return "cart/cartList";
    }

    /**
     * 장바구니 업데이트처리 메소드
     *
     * @param cartItemId 장바구니 상품 아이디
     * @param count 장바구니 상품의 개수
     * @param principal 현재 로그인한 회원
     *
     * @return 생성된 장바구니 상품 아이디<br>
     * HttpStatus.OK 요청이 성공했다는 http 응답 상태코드 반환
     */
    @Operation(summary = "장바구니 업데이트처리 메소드", description = "장바구니 상품 개수 업데이트처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "상품 개수 반환 실패"),
            @ApiResponse(responseCode = "403", description = "권한없음, 업데이트 실패"),
            @ApiResponse(responseCode = "200", description = "장바구니 상품 개수 업데이트 성공")
    })
    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@Parameter(description = "장바구니 상품 아이디") @PathVariable("cartItemId") Long cartItemId, int count, Principal principal) {
        if(count <= 0) {
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        } else if(!cartService.validateCartItem(cartItemId, principal.getName())) {
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    /**
     * 장바구니 삭제처리 메소드
     *
     * @param cartItemId 장바구니 상품 아이디
     * @param principal 현재 로그인한 회원
     *
     * @return 생성된 장바구니 상품 아이디 <br>
     * HttpStatus.OK 요청이 성공했다는 http 응답 상태코드 반환
     */
    @Operation(summary = "장바구니 삭제처리 메소드", description = "장바구니 상품 삭제처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 상품 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한없음, 장바구니 상품 삭제 실패")
    })
    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@Parameter(description = "장바구니 상품 아이디") @PathVariable("cartItemId") Long cartItemId, Principal principal) {
        if(!cartService.validateCartItem(cartItemId, principal.getName())) {
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    /**
     * 장바구니 상품 주문처리 메소드
     *
     * @param cartOrderDto 장바구니의 상품을 주문할 정보를 담은 객체
     * @param principal 현재 로그인한 회원
     *
     * @return 주문 아이디 <br>
     * HttpStatus.OK 요청이 성공했다는 http 응답 상태코드 반환
     */
    @Operation(summary = "장바구니 상품 주문처리 메소드", description = "장바구니 상품 주문처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "권한없음, 주문실패"),
            @ApiResponse(responseCode = "200", description = "장바구니 상품 주문 호출")
    })
    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal) {
        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0) {
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }

        for(CartOrderDto cartOrder : cartOrderDtoList) {
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())) {
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId;

        try {
            orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName(), cartOrderDto.getUsedPoint());
        } catch(Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return  new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}
