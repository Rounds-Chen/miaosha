package com.example.miaosha.service.impl;

import com.example.miaosha.dao.ItemDtoMapper;
import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.dto.ItemDto;
import com.example.miaosha.dto.ItemStockDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.service.model.PromoModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ItemDtoMapper itemDtoMapper;

    @Autowired
    ItemStockDtoMapper itemStockDtoMapper;

    @Autowired
    PromoService promoService;

    @Resource
    RedisUtil redisUtil;

    @Resource
    RedisTemplate redisTemplate;

    @Override
    public ItemModel createItem(ItemModel item) throws BussinessException {
        ItemDto itemDto = this.convertItemDtoFromItemModel(item);
        itemDtoMapper.insertSelective(itemDto);

        ItemStockDto itemStockDto = this.convertItemStockDtoFromItemModel(item);
        itemStockDtoMapper.insertSelective(itemStockDto);

        return item;
    }

    @Override
    public List<ItemModel> itemList() {
        List<ItemDto> list = itemDtoMapper.itemList();

        List<ItemModel> itemModelList = list.stream().map(itemDto -> {
            ItemStockDto itemStockDto = itemStockDtoMapper.selectByItemId(itemDto.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDto, itemStockDto);
            PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
            if (promoModel != null && promoModel.getStatus() != 3) itemModel.setPromoModel(promoModel);

            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItem(Integer id) throws BussinessException {
        ItemDto itemDto = itemDtoMapper.selectByPrimaryKey(id);
        if (itemDto == null) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }

        ItemStockDto itemStockDto = itemStockDtoMapper.selectByItemId(itemDto.getId());
        if (itemStockDto == null) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");

        }
        ItemModel itemModel = this.convertModelFromDataObject(itemDto, itemStockDto);
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) itemModel.setPromoModel(promoModel);

        return itemModel;
    }

    @Override
    public ItemModel getItemInCache(Integer id) throws BussinessException {
        String itemKey = CacheConstant.ORDER_ITEM_CACHE_PREFIX + id;
        ItemModel itemModel = redisUtil.getCacheObject(itemKey);
        if (itemModel == null) {
            itemModel = this.getItem(id);
            redisUtil.setCacheObjectExpire(itemKey, itemModel, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int stock = itemStockDtoMapper.decreaseStock(itemId, amount);
        return stock >= 0;
    }

    @Override
    public boolean decreaseStockInCache(Integer itemId, Integer amout) {
        // 无法使用redis乐观锁 因为createOrderByTransication需要被@Transication修饰 和redis 事务不一致 不支持操作
//        String stockKey=CacheConstant.ITEM_STOCK_CACHE_PREFIX+itemId;
//        while(true){
//            redisUtil.watchKey(stockKey);
//            try{
//                redisUtil.startMulti();
//
//                int stock=(Integer) redisUtil.getCacheObject(stockKey);
//                if(stock<amout){
//                    return false;
//                }
//
//                redisUtil.setCacheObject(stockKey,stock-amout);
//                List<Object> results = redisUtil.execMulti();
//
//                if(results==null||results.size()==0){
//                    waitForLock();
//                }else{
//                    return true;
//                }
//            }catch (Exception e){
//                System.out.println(e);
//            }
//            finally {
//                redisUtil.unwatchKey();
//            }
//        }
        String stockKey = CacheConstant.ITEM_STOCK_CACHE_PREFIX + itemId;
        long res = redisUtil.incrementCacheObject(stockKey, -1 * amout);
        return res >= 0;
    }


    @Override
    @Transactional
    public void increaseSalesInCache(Integer itemId, Integer amount) {
//        itemDtoMapper.increaseSales(itemId, amount);
        redisUtil.incrementCacheObject(CacheConstant.ITEM_SALES_CACHE_PREFIX+itemId,amount);

    }

    private ItemDto convertItemDtoFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDto itemDto = new ItemDto();
        BeanUtils.copyProperties(itemModel, itemDto);
        return itemDto;
    }

    private ItemStockDto convertItemStockDtoFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDto itemStockDto = new ItemStockDto();
        itemStockDto.setItemId(itemModel.getId());
        itemStockDto.setStock(itemModel.getStock());

        return itemStockDto;
    }

    private ItemModel convertModelFromDataObject(ItemDto itemDto, ItemStockDto itemStockDto) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDto, itemModel);
        itemModel.setStock(itemStockDto.getStock());
        return itemModel;
    }
}
