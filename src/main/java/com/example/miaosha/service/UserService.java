package com.example.miaosha.service;

import com.example.miaosha.dto.UserDto;
import com.example.miaosha.service.model.UserModel;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserModel getUserById(Integer id);

}
