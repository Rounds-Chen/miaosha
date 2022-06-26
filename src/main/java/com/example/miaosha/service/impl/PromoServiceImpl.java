package com.example.miaosha.service.impl;

import com.example.miaosha.dao.PromoDtoMapper;
import com.example.miaosha.dto.PromoDto;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDtoMapper promoDtoMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDto promoDto=promoDtoMapper.selectByItemId(itemId);
        PromoModel promoModel=convertFromDataObject(promoDto);
        if(promoModel==null) return null;

        DateTime now=DateTime.now();
        if(promoModel.getStartTime().isAfter(now)){
            promoModel.setStatus(1);
        }else if(promoModel.getEndTime().isBefore(now)){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public PromoModel getPromoById(Integer promoId) {
        PromoDto promoDto=promoDtoMapper.selectByPrimaryKey(promoId);
        return convertFromDataObject(promoDto);
    }

    private PromoModel convertFromDataObject(PromoDto promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setStartTime(new DateTime(promoDO.getStartDate()));
        promoModel.setEndTime(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }
}
