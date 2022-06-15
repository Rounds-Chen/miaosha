package com.example.miaosha.service.impl;

import com.example.miaosha.dao.UserDtoMapper;
import com.example.miaosha.dao.UserPasswordDtoMapper;
import com.example.miaosha.dto.UserDto;
import com.example.miaosha.dto.UserPasswordDto;
import com.example.miaosha.service.UserService;
import com.example.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDtoMapper userDtoMapper;

    @Autowired
    UserPasswordDtoMapper userPasswordDtoMapper;

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
