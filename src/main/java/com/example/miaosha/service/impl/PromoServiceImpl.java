package com.example.miaosha.service.impl;

import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.dao.PromoDtoMapper;
import com.example.miaosha.dto.ItemStockDto;
import com.example.miaosha.dto.PromoDto;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.model.PromoModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDtoMapper promoDtoMapper;

    @Resource
    RedisUtil redisUtil;

    @Autowired
    ItemStockDtoMapper itemStockDtoMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDto promoDto = promoDtoMapper.selectByItemId(itemId);
        PromoModel promoModel = convertFromDataObject(promoDto);
        if (promoModel == null) return null;

        DateTime now = DateTime.now();
        if (promoModel.getStartTime().isAfter(now)) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndTime().isBefore(now)) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public PromoModel getPromoById(Integer promoId) {
        PromoDto promoDto = promoDtoMapper.selectByPrimaryKey(promoId);
        return convertFromDataObject(promoDto);
    }

    @Override
    public void publishPromoById(Integer promoId) {
        PromoModel promoModel=this.getPromoById(promoId);
        if(promoModel==null) return ;

        ItemStockDto itemStockDto=itemStockDtoMapper.selectByItemId(promoModel.getItemId());
        String stockKey= CacheConstant.ITEM_STOCK_CACHE_PREFIX+itemStockDto.getItemId();

        redisUtil.setCacheObject(stockKey, itemStockDto.getStock(),10, TimeUnit.MINUTES);
    }

    private PromoModel convertFromDataObject(PromoDto promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoPrice(promoDO.getPromoItemPrice());
        promoModel.setStartTime(new DateTime(promoDO.getStartDate()));
        promoModel.setEndTime(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }
}
