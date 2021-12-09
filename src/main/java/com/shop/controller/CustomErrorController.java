package com.shop.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping(value = "/error")
    public String error(HttpServletRequest request, Model model) {
        Object statusCodeObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if(statusCodeObj != null) {
            int statusCode = Integer.valueOf(statusCodeObj.toString());

            model.addAttribute("statusCode", statusCode);
        }

        return "error";
    }

}