package com.shop.service;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니 서비스
 *
 * @author 공통
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    /**
     * 장바구니 상품 담기
     *
     * @param cartItemDto 장바구니에 담을 상품 정보를 담은 객체
     * @param email 현재 로그인한 회원의 이메일(아이디)
     *
     * @return savedCartItem.getId() 장바구니에 있던 상품일 경우, 장바구니에 저장된 상품 ID 반환
     * @return cartItem.getId() 장바구니에 들어갈 새 상품 ID 반환
     */
    public Long addCart(CartItemDto cartItemDto, String email) {
        Item item = itemRepository.findById(cartItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());

        if(cart == null) {
            cart = Cart.createCart(member);

            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if(savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount());

            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());

            cartItemRepository.save(cartItem);

            return cartItem.getId();
        }
    }

    /**
     * 장바구니에 들어 있는 상품 조회
     *
     * @param email 현재 로그인한 회원의 이메일(아이디)
     *
     * @return cartDetailDtoList 장바구니에 담긴 상품 목록 반환
     *
     */
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());

        if(cart == null) {
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartDetailDtoList;
    }

    /**
     * 현재 로그인한 회원과 장바구니 상품을 저장한 회원이 동일한지 검사
     *
     * @param cartItemId 장바구니에 저장된 상품 아이디
     * @param email 현재 로그인한 회원의 이메일(아이디)
     *
     * @return boolean 현재 로그인한 회원과 장바구니 상품을 저장한 회원이 다를경우 false,같으면 true
     */
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Member curMember = memberRepository.findByEmail(email);
        Member savedMember = cartItem.getCart().getMember();

        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }

        return true;
    }

    /**
     * 장바구니 상품의 수량 업데이트 메소드
     *
     * @param cartItemId 장바구니에 저장된 상품 아이디
     * @param count 현재 로그인한 회원의 이메일(아이디)
     *
     * @return 장바구니의 수량 변경
     */
    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        cartItem.updateCount(count);
    }

    /**
     * 장바구니에 저장된 상품 삭제 메소드
     *
     * @param cartItemId 장바구니에 저장된 상품 아이디
     *
     * @return
     */
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        cartItemRepository.delete(cartItem);
    }

    /**
     * 주문한 상품은 장바구니에서 제거
     *
     * @param cartOrderDtoList 장바구니에 저장된 상품 아이디
     * @param email 현재 로그인한 회원의 이메일(아이디)
     *
     * @return orderId 주문한 상품 장바구니에서 제거후 주문아이디 반환
     */
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email, Integer usedPoint) {
        List<OrderDto> orderDtoList = new ArrayList<>();

        for(CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email, usedPoint);

        for(CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);

            cartItemRepository.delete(cartItem);
        }

        return orderId;
    }

}
