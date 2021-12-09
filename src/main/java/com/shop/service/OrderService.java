package com.shop.service;

import com.shop.constant.GiftStatus;
import com.shop.constant.NoticeType;
import com.shop.constant.OrderStatus;
import com.shop.constant.ReturnStatus;
import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Tag(name = "주문 서비스", description = "주문 서비스 목록")
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final ItemImgRepository itemImgRepository;
    private final ItemTagRepository itemTagRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    public void processPointUsage(Member member, Order order) {
        member.setPoint(member.getPoint() - order.getUsedPoint() + order.getAccPoint());

        memberRepository.save(member);
    }

    public void processTagTotalSell(Item item) {
        List<ItemTag> itemTag = itemTagRepository.findByItemId(item.getId());

        for(ItemTag itemtag : itemTag) {
            itemtag.getTag().addTotalSell();
        }
    }

    public Long order(OrderDto orderDto, String email) {
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

        Member member = memberRepository.findByEmail(email);

        if(member.getPoint() < orderDto.getUsedPoint()) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        List<OrderItem> orderItemList = new ArrayList<>();

        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);

        Order order = Order.createOrder(member, orderDto, orderItemList);

        this.processTagTotalSell(item);
        this.processPointUsage(member, order);

        if(orderDto.getGiftStatus().equals(GiftStatus.BUY)) {
            if(member.getNoticeType().equals(NoticeType.EMAIL)) {
                emailService.sendOrderEmail(member.getEmail(), orderDto);
            } else if(member.getNoticeType().equals(NoticeType.SMS)) {
                smsService.sendOrderSms(member.getPhone(), orderDto);
            }
        }

        orderRepository.save(order);

        return order.getId();
    }

    public Long orders(List<OrderDto> orderDtoList, String email, Integer usedPoint) {
        Member member = memberRepository.findByEmail(email);

        if(member.getPoint() < usedPoint) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        List<OrderItem> orderItemList = new ArrayList<>();

        for(OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());

            orderItemList.add(orderItem);
        }

        OrderDto orderDto = new OrderDto();
        orderDto.setAddress(member.getAddress());
        orderDto.setAddressDetail(member.getAddressDetail());
        orderDto.setUsedPoint(usedPoint);
        orderDto.setGiftStatus(GiftStatus.BUY);

        Order order = Order.createOrder(member, orderDto, orderItemList);

        orderRepository.save(order);

        this.processPointUsage(member, order);

        if(member.getNoticeType().equals(NoticeType.EMAIL)) {
            emailService.sendCartOrderEmail(member.getEmail(), orderDtoList, order.getTotalPrice());
        } else if(member.getNoticeType().equals(NoticeType.SMS)) {
            smsService.sendCartOrderSms(member.getPhone(), orderDtoList, order.getTotalPrice());
        }

        for(OrderItem orderItem : orderItemList) {
            this.processTagTotalSell(orderItem.getItem());
        }

        return order.getId();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.getById(orderId);
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        return this.getPaginatedOrderList(orders, pageable, totalCount);
    }

    /**
     * 구매/선물 리스트 조회
     *
     * @param email 해당 회원의 이메일
     * @param giftStatus 구매/선물 상태
     * @param pageable 페이징 처리에 필요한 정보
     *
     * @return 페이징 처리된 구매/선물 리스트
     */

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderListStatus(String email, Pageable pageable, GiftStatus giftStatus) {
        List<Order> orders = orderRepository.findOrdersStatus(email, pageable, giftStatus);
        Long totalCount = orderRepository.countOrder(email);

        return this.getPaginatedOrderList(orders, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getReturnList(String email, Pageable pageable) {
        List<Order> orders = orderRepository.findOrdersForReturnList(email, pageable);
        Long totalCount = orderRepository.countOrderForReturnList(email);

        return this.getPaginatedOrderList(orders, pageable, totalCount);
    }

    private Page<OrderHistDto> getPaginatedOrderList(List<Order> orders, Pageable pageable, Long totalCount) {
        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);

            List<OrderItem> orderItems = order.getOrderItems();

            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");

                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());

                orderHistDto.addOrderItemDto(orderItemDto);
            }

            orderHistDtos.add(orderHistDto);
        }

        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        Member curMember = memberRepository.findByEmail(email);
        Member savedMember = order.getMember();

        return StringUtils.equals(curMember.getEmail(), savedMember.getEmail());
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();
    }

    public void requestReturn(Order order) {
        List<OrderItem> orderItemList = order.getOrderItems();

        for (OrderItem orderItem : orderItemList) {
            orderItem.setReturnReqDate(LocalDateTime.now());
            orderItem.setReturnPrice(orderItem.getOrderPrice());
            orderItem.setReturnCount(orderItem.getCount());
            orderItem.setReturnStatus(ReturnStatus.N);

            orderItemRepository.save(orderItem);
        }

        order.setReturnReqDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.RETURN);
        order.setReturnStatus(ReturnStatus.N);

        orderRepository.save(order);
    }

    public void confirmReturn(Long orderId) {
        Order order = this.getOrder(orderId);

        List<OrderItem> orderItemList = order.getOrderItems();

        for (OrderItem orderItem : orderItemList) {
            orderItem.setReturnConfirmDate(LocalDateTime.now());
            orderItem.setReturnPrice(orderItem.getOrderPrice());
            orderItem.setReturnCount(orderItem.getCount());
            orderItem.setReturnStatus(ReturnStatus.Y);

            orderItemRepository.save(orderItem);
        }

        order.setReturnConfirmDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.RETURN);
        order.setReturnStatus(ReturnStatus.Y);

        orderRepository.save(order);

        Member member = order.getMember();

        // 반품 포인트 회수
        member.setPoint(member.getPoint() + order.getUsedPoint() - order.getAccPoint());

        memberRepository.save(member);
    }

}
