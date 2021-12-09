package com.shop.controller;

import com.shop.dto.BestItemDto;
import com.shop.service.BestItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/item/best")
@RequiredArgsConstructor
public class BestItemController {

    private final BestItemService bestItemService;

    @GetMapping("/day")
    public String getBestItemByDays(Model model) {
        List<BestItemDto> items = bestItemService.getBestOfDayItem();

        model.addAttribute("title", "일간");
        model.addAttribute("items", items);

        return "item/bestItem";
    }

    @GetMapping(value = "/week")
    public String getBestItemByWeek(Model model){
        List<BestItemDto> items = bestItemService.getBestOfWeekItem();

        model.addAttribute("title", "주간");
        model.addAttribute("items", items);

        return "item/bestItem";
    }

    @GetMapping(value = "/month")
    public String getBestItemByMonth(Model model){
        List<BestItemDto> items = bestItemService.getBestOfMonthItem();

        model.addAttribute("title", "월간");
        model.addAttribute("items", items);

        return "item/bestItem";
    }

}