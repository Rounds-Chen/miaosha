package com.example.miaosha.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.example.miaosha.dao.UserDtoMapper;
import com.example.miaosha.dao.UserPasswordDtoMapper;
import com.example.miaosha.dto.UserDto;
import com.example.miaosha.dto.UserPasswordDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.service.UserService;
import com.example.miaosha.service.model.UserModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDtoMapper userDtoMapper;

    @Autowired
    UserPasswordDtoMapper userPasswordDtoMapper;

    @Resource
    RedisUtil redisUtil;

    @Override
    public UserModel getUserById(Integer id) {

        UserDto userDO = userDtoMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        }

        //通过用户id获取对应的用户加密密码信息
        UserPasswordDto userPasswordDO = userPasswordDtoMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO, userPasswordDO);

    }

    @Override
    public UserModel getUserInCacheById(Integer id) {
        String userKey= CacheConstant.USER_CACHE_PREFIX+id;
        UserModel userModel=redisUtil.getCacheObject(userKey);
        if(userModel==null){
            userModel=this.getUserById(id);
            redisUtil.setCacheObjectExpire(userKey,userModel,10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    @Override
    @Transactional//声明事务
    public void register(UserModel userModel) throws BussinessException {
                if (userModel == null) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserDto userDto = convertFromModel(userModel);

        try {
            userDtoMapper.insertSelective(userDto);
        }catch (DuplicateKeyException ex){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"手机号已注册");
        }

        userModel.setId(userDto.getId());
        UserPasswordDto userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDtoMapper.insertSelective(userPasswordDO);

    }

    @Override
    public UserModel login(String telephone, String password) throws BussinessException {
        UserDto userDto=userDtoMapper.selectByTelphoneUserDto(telephone);

        if(userDto==null){
            throw new BussinessException(EmBussinessError.USER_LOOGIN_FAIL);
        }
        UserPasswordDto userPasswordDto=userPasswordDtoMapper.selectByUserId(userDto.getId());
        UserModel userModel=convertFromDataObject(userDto,userPasswordDto);

        if(!StringUtils.equals(userModel.getEncrptPassword(),password)){
            throw new BussinessException(EmBussinessError.USER_LOOGIN_FAIL);
        }

        return userModel;
    }

    private UserPasswordDto convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDto userPasswordDto = new UserPasswordDto();
        userPasswordDto.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDto.setUserId(userModel.getId());

        return userPasswordDto;
    }

    private UserDto convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userModel, userDto);
        return userDto;
    }


    private UserModel convertFromDataObject(UserDto userDO, UserPasswordDto userPasswordDO) {
        if (userDO == null) {
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);

        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;

    }
}
