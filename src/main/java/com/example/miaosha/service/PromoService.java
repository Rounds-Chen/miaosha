package com.example.miaosha.service;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.service.model.PromoModel;
import org.springframework.stereotype.Service;

@Service
public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);

    PromoModel getPromoById(Integer promoId);

    void publishPromoById(Integer promoId) throws BussinessException;

    String generatePromoToken(Integer promoId,Integer itemId,Integer userId) throws BussinessException;
}
