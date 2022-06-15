package com.example.miaosha.service;

import com.example.miaosha.dto.UserDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.service.model.UserModel;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BussinessException;

    UserModel login(String telephone,String password) throws BussinessException;
}
