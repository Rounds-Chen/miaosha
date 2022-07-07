package com.example.miaosha.service.impl;

import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.dao.PromoDtoMapper;
import com.example.miaosha.dto.ItemStockDto;
import com.example.miaosha.dto.PromoDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.UserService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.service.model.PromoModel;
import com.example.miaosha.service.model.UserModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDtoMapper promoDtoMapper;

    @Resource
    RedisUtil redisUtil;

    @Autowired
    ItemStockDtoMapper itemStockDtoMapper;

    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

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
        String doorkey=CacheConstant.PROMO_DOOR_PREFIX+"promoId_"+promoId+"_itemId_"+itemStockDto.getItemId();

        redisUtil.setCacheObject(stockKey, itemStockDto.getStock());
        redisUtil.setCacheObject(doorkey,itemStockDto.getStock()*3);
    }

    @Override
    public String generatePromoToken(Integer promoId, Integer itemId, Integer userId) throws BussinessException {
        // 检查库存是否足够
        String stockKey= CacheConstant.ITEM_STOCK_CACHE_PREFIX+itemId;
        if((Integer)redisUtil.getCacheObject(stockKey)<=0){
           return null;
        }

        // a. 用户商品信息校验
        UserModel userModel = userService.getUserInCacheById(userId);
        if (userModel == null) {
            return null;
        }
        ItemModel itemModel = itemService.getItemInCache(itemId);
        if (itemModel == null) {
            return  null;
        }
        // b.活动信息校验
        PromoModel promoModel = itemModel.getPromoModel();
        if (promoModel == null || !Objects.equals(promoId, promoModel.getId())) {
            return null;
        } else if (promoModel.getStatus() != 2) {
            return null;
        }

        // 令牌不足直接返回
        String doorKey=CacheConstant.PROMO_DOOR_PREFIX+"promoId_"+promoId+"_itemId_"+itemId;
        if(redisUtil.incrementCacheObject(doorKey,-1)<0){
            return null;
        }
        String promoToken= UUID.randomUUID().toString().replace("-","");
        String tokenPrefix=CacheConstant.PROMO_TOKEN_PREFIX+"promoId_"+promoId+"_userId_"+userId+"_itemId_"+itemId;
        redisUtil.setCacheObjectExpire(tokenPrefix,promoToken,5,TimeUnit.MINUTES);

        return promoToken;
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
