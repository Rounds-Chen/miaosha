package com.example.miaosha.controller;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.OrderService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.model.OrderModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    PromoService promoService;

    @Resource
    RedisUtil redisUtil;

    @PostMapping("/generateToken")
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam("promoId") Integer promoId,
                                          @RequestParam("itemId") Integer itemId) throws BussinessException {
        Integer userId= Integer.parseInt((String)(httpServletRequest.getAttribute("userId")));
        String token=promoService.generatePromoToken(promoId,itemId,userId);
        if(token==null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }

        return CommonReturnType.create(token);
    }

    @PostMapping("/createOrder")
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam("promoId") Integer promoId,
                                        @RequestParam("promoToken") String promoToken) throws BussinessException {
        Integer userId= Integer.parseInt((String)(httpServletRequest.getAttribute("userId")));

        String tokenKey=CacheConstant.PROMO_TOKEN_PREFIX+"promoId_"+promoId+"_userId_"+userId+"_itemId_"+itemId;
        String promoInCache=redisUtil.getCacheObject(tokenKey);
        if(promoInCache==null|| !StringUtils.equals(promoInCache,promoToken)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"令牌校验失败");
        }

        OrderModel orderModel=orderService.createByTransication(userId,itemId,amount,promoId);
        return CommonReturnType.create(orderModel);
    }
}
