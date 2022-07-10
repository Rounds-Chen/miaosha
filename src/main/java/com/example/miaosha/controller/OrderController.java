package com.example.miaosha.controller;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.OrderService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.model.OrderModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@CrossOrigin
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

    @Autowired
    DefaultKaptcha defaultKaptcha;

    private RateLimiter rateLimiter;

    @PostConstruct
    public void init(){
        rateLimiter=RateLimiter.create(300);
    }

    @PostMapping("/generateToken")
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam("promoId") Integer promoId,
                                          @RequestParam("itemId") Integer itemId,
                                          @RequestParam("verifyCode")String verifyCode) throws BussinessException {
        Integer userId= Integer.parseInt((String)(httpServletRequest.getAttribute("userId")));

        String codeInCache=redisUtil.getCacheObject(CacheConstant.VERIFY_CODE_PREFIX+userId);
        if(codeInCache==null||!StringUtils.equals(codeInCache,verifyCode)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"图片验证码校验失败");
        }

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
                                        @RequestParam("promoId") Integer promoId
                                        ) throws BussinessException, ExecutionException, InterruptedException {
        Integer userId= Integer.parseInt((String)(httpServletRequest.getAttribute("userId")));

//        String tokenKey=CacheConstant.PROMO_TOKEN_PREFIX+"promoId_"+promoId+"_userId_"+userId+"_itemId_"+itemId;
//        String promoInCache=redisUtil.getCacheObject(tokenKey);
//        if(promoInCache==null|| !StringUtils.equals(promoInCache,promoToken)){
//            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"令牌校验失败");
//        }

//        try{
//            rateLimiter.tryAcquire(1);
//        }catch (Exception e){
//            throw new BussinessException(EmBussinessError.RATELIMITER_ERROR);
//        }

        OrderModel orderModel=orderService.createByTransication(userId,itemId,amount,promoId);
        return CommonReturnType.create(orderModel);
    }

    @GetMapping("/generateVerifyCode")
    @ResponseBody
    public CommonReturnType generateVerifyCode(HttpServletResponse response) throws IOException {
        int userId= Integer.parseInt((String)(httpServletRequest.getAttribute("userId")));

        String codeText = defaultKaptcha.createText();
        BufferedImage codeImage = defaultKaptcha.createImage(codeText);
        ImageIO.write(codeImage, "jpg", response.getOutputStream());

        redisUtil.setCacheObjectExpire(CacheConstant.VERIFY_CODE_PREFIX+userId,codeText,5, TimeUnit.MINUTES);
        return CommonReturnType.create(null);
    }
}
