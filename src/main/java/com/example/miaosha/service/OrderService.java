package com.example.miaosha.service;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.service.model.OrderModel;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    OrderModel create(Integer userId, Integer itemId, Integer amount,Integer promoId) throws BussinessException;
}


