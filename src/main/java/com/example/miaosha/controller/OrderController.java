package com.example.miaosha.controller;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.OrderService;
import com.example.miaosha.service.model.OrderModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    OrderService orderService;

    @Resource
    RedisUtil redisUtil;

    @PostMapping("/createOrder")
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam("promoId") Integer promoId) throws BussinessException {
        Integer userId= Integer.parseInt((String)(httpServletRequest.getAttribute("userId")));
//        OrderModel orderModel = orderService.create(userId, itemId, amount,promoId);
        String stockKey= CacheConstant.ITEM_STOCK_CACHE_PREFIX+itemId;
        // 库存不足
        if(Integer.parseInt(redisUtil.getCacheObject(stockKey))<=0){
            throw new BussinessException(EmBussinessError.STOCK_NOT_ENOUGH);
        }
        OrderModel orderModel=orderService.createByTransication(userId,itemId,amount,promoId);
        return CommonReturnType.create(orderModel);
    }
}
