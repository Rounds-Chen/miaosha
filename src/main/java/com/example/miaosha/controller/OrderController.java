package com.example.miaosha.controller;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.OrderService;
import com.example.miaosha.service.model.OrderModel;
import com.example.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    OrderService orderService;

    @PostMapping("/createOrder")
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam("promoId") Integer promoId) throws BussinessException {
        获取用户登录信息
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN, "用户还未登录，不能下单");
        }
        Integer userId = (Integer) httpServletRequest.getSession().getAttribute("LOGIN_USER_ID");
            Integer userId=58;

        OrderModel orderModel = orderService.create(userId, itemId, amount,promoId);

        return CommonReturnType.create(orderModel);
    }
}
