package com.example.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.example.miaosha.controller.viewobject.UserVO;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.JwtService;
import com.example.miaosha.service.UserService;
import com.example.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import java.util.Base64;


@CrossOrigin
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @Resource
    JwtService jwtService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @RequestMapping("/get")
    @ResponseBody
    public UserVO getUser(@RequestParam("id") Integer id) {
        UserModel userModel = userService.getUserById(id);

        if (userModel == null) {
            return null;
        }
        UserVO userVO = convertFromModel(userModel);

        return userVO;
    }

    /*
     * 生成验证码
     */
    @PostMapping("/getOtp")
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam("telephone") String telephone) {
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码通过短信通道发送给用户，省略
        System.out.println("telphone=" + telephone + "&otpCode=" + otpCode);
        return CommonReturnType.create(null);
    }

    /*
     * 用户注册
     */
    @PostMapping("/register")
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") String gender,
                                     @RequestParam(name = "age") String age,
                                     @RequestParam(name = "password") String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 验证手机号和otp是否一致
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }

        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(Integer.valueOf(age));
        userModel.setGender(Byte.valueOf(gender));
        userModel.setTelphone(telphone);
        userModel.setRegisitMode("byphone");

        //密码加密
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    /*
     * 用户登录
     */
    @PostMapping("/login")
    @ResponseBody
    public CommonReturnType login(@RequestParam("telephone") String telephone, @RequestParam("password") String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telephone) || StringUtils.isEmpty(password)) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }

        UserModel userModel = userService.login(telephone, this.EncodeByMd5(password));
        Integer userId=userModel.getId();
        String token=jwtService.generateToken(String.valueOf(userId));
        token="bearer;"+token;

        return CommonReturnType.create(token);
    }

    private String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64.Encoder base64en = Base64.getEncoder();
        //加密字符串
        byte[] newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return new String(newstr, "utf-8");
    }


    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;

    }

}
