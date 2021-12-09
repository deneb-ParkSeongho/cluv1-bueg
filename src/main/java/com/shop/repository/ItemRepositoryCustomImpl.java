package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemComplexSearchSortColumn;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.*;
import com.shop.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 사용자 정의 레포지토리 구현
 *
 * @author 공통
 * @version 1.0
 */
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 상품 판매 상태 따른 조건 생성
     * @param searchSellStatus  상품 판매 상태 조건
     * @return 상태가 전체(NULL)일 경우 NULL을 리턴해 해당 조건 무시
     */
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    /**
     * 검색 시간 조건에 따른 조건 생성
     * @param searchDateType  검색 시간 조건
     * @return 해당 시간 이후로 등록된 상품만 조회
     */
    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if(StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if(StringUtils.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if(StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        } else if(StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }

        return QItem.item.regTime.after(dateTime);
    }

    /**
     * 입력 값에 따른 포함항목 검색 조건
     * @param searchBy  상품 검색 조건
     * @param searchQuery
     * @return
     */
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if(StringUtils.equals("itemNm", searchBy)) {
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("createdBy", searchBy)) {
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    private OrderSpecifier searchOrderBy(ItemComplexSearchDto itemComplexSearchDto) {
        ItemComplexSearchSortColumn sortColumn = itemComplexSearchDto.getSortColumn();
        Sort.Direction sortDirection = itemComplexSearchDto.getSortDirection();

        OrderSpecifier orderSpecifier = null;

        if(sortColumn.equals(ItemComplexSearchSortColumn.REG_TIME)) {
            if(sortDirection.isAscending()) {
                orderSpecifier = QItem.item.regTime.asc();
            } else {
                orderSpecifier = QItem.item.regTime.desc();
            }
        } else if(sortColumn.equals(ItemComplexSearchSortColumn.NAME)) {
            if(sortDirection.isAscending()) {
                orderSpecifier = QItem.item.itemNm.asc();
            } else {
                orderSpecifier = QItem.item.itemNm.desc();
            }
        } else if(sortColumn.equals(ItemComplexSearchSortColumn.PRICE)) {
            if(sortDirection.isAscending()) {
                orderSpecifier = QItem.item.price.asc();
            } else {
                orderSpecifier = QItem.item.price.desc();
            }
        }

        return orderSpecifier;
    }

    private BooleanExpression searchCategory(ItemComplexSearchDto itemComplexSearchDto) {
        Long cateCode = itemComplexSearchDto.getSearchCategory();

        return cateCode == null ? null : QItem.item.category.cateCode.eq(cateCode);
    }

    private BooleanExpression searchTag(ItemComplexSearchDto itemComplexSearchDto) {
        List<Long> tagIds = itemComplexSearchDto.getSearchTagIds();

        return tagIds == null || tagIds.isEmpty() ? null : QItemTag.itemTag.tag.id.in(tagIds);
    }

    private BooleanExpression itemNmLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    /**
     * 조회조건과 페이징 정보에 따라 상품정보를 찾는 쿼리 메소드(상품관리페이지)
     * @param itemSearchDto 상품 조회 조건
     * @param pageable 페이징 정보
     * @return PageImpl(content, pageable, total)
     *         조회조건과 페이징 정보에 따른 상품 정보 반환
     */
    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QueryResults<Item> results = queryFactory
                .selectFrom(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Item> content = results.getResults();

        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 조회조건과 페이징 정보에 따라 상품정보를 찾는 쿼리 메소드(메인페이지)
     * @param itemSearchDto 상품 조회 조건
     * @param pageable 페이징 정보
     * @return PageImpl<>(content, pageable, total)
     *         조회조건과 페이징 정보에 따른 상품 정보 반환
     */
    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price,
                                item.shippingFee
                        )
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();

        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<GiftMainItemDto> getGiftItemPage(ItemSearchDto itemSearchDto, Pageable pageable, Long cateCode) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        QCategory category = QCategory.category;

        QueryResults<GiftMainItemDto> results = queryFactory
                .select(
                        new QGiftMainItemDto(
                                item.id,
                                category.cateCode,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .join(item.category, category)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .where(category.cateCode.eq(cateCode))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<GiftMainItemDto> content = results.getResults();

        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 조회조건과 페이징 정보, 태그에 따라 상품정보를 찾는 쿼리 메소드(태그검색 페이지)
     * @param itemSearchDto 상품 조회 조건
     * @param pageable 페이징 정보
     * @param filters 적용된 태그들
     * @return PageImpl<>(content, pageable, total)
     *         조회조건과 페이징 정보에 따른 상품 정보 반환
     */
    public Page<MainItemDto> getDetailSearchPage(String[] filters, ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        QTag tag = QTag.tag;
        com.shop.entity.QItemTag itemTag = com.shop.entity.QItemTag.itemTag;

        List<String> filterList = Arrays.stream(filters).collect(Collectors.toList());

        JPQLQuery<MainItemDto> query = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price,
                                item.shippingFee
                        )

                )
                .from(itemImg)
                .join(itemImg.item, item)
                .leftJoin(itemTag).on(itemTag.item.eq(itemImg.item))
                .leftJoin(itemTag.tag, tag)
                .where(itemImg.repImgYn.eq("Y"))        //상품 이미지 경우 대표 상품 이미지만 불러옴
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .groupBy(itemImg.item)
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if(filterList.size() > 0) {
            query.where(tag.tagNm.in(filterList));
        }

        QueryResults<MainItemDto> results = query.fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    private List<BestItemDto> getBestItem(Integer d) {
        QItem item = QItem.item;
        QOrderItem orderItem = QOrderItem.orderItem;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<BestItemDto> results = queryFactory
                .select(
                        new QBestItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price
                        )
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .leftJoin(orderItem).on(
                        orderItem.item.eq(itemImg.item)
                                .and(orderItem.regTime.goe(LocalDateTime.now().minusDays(d)))
                )
                .where(itemImg.repImgYn.eq("Y"))
                .groupBy(item.id)
                .orderBy(orderItem.count().desc())
                .orderBy(item.id.desc())
                .fetchResults();

        List<BestItemDto> content = results.getResults();

        return content;
    }

    @Override
    public List<BestItemDto> getBestOfDayItem() {
        return this.getBestItem(1);
    }

    @Override
    public List<BestItemDto> getBestOfWeekItem() {
        return this.getBestItem(7);
    }

    @Override
    public List<BestItemDto> getBestOfMonthItem() {
        return this.getBestItem(30);
    }

    @Override
    public Page<MainItemDto> getComplexSearchPage(ItemComplexSearchDto itemComplexSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        QItemTag itemTag = QItemTag.itemTag;
        QTag tag = QTag.tag;

        QueryResults<MainItemDto> results = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price,
                                item.shippingFee
                        )
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .leftJoin(itemTag).on(itemTag.item.eq(itemImg.item))
                .leftJoin(itemTag.tag, tag)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNmLike(itemComplexSearchDto.getSearchQuery()))
                .where(searchCategory(itemComplexSearchDto))
                .where(searchTag(itemComplexSearchDto))
                .groupBy(itemImg.item)
                .orderBy(searchOrderBy(itemComplexSearchDto))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();

        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }
    
}
