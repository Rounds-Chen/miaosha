package com.example.miaosha.controller;

import com.example.miaosha.controller.viewobject.ItemVO;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.service.model.PromoModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.CommonCacheUtil;
import com.example.miaosha.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@CrossOrigin
@Controller
@RequestMapping("/item")
public class ItemController extends BaseController{

    @Autowired
    ItemService itemService;

    @Resource
    RedisUtil redisUtil;

    @Resource
    CommonCacheUtil cacheUtil;

    @Autowired
    PromoService promoService;


    @PostMapping("/create")
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BussinessException {
        ItemModel itemModel=new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel item=itemService.createItem(itemModel);
        ItemVO itemVO=this.convertVOFromModel(item);
        return CommonReturnType.create(itemVO);
    }

    @GetMapping("/list")
    @ResponseBody
    public CommonReturnType itemList(){
        List<ItemModel> itemModelList=itemService.itemList();
        List<ItemVO> list=itemModelList.stream().map(itemModel->{
             ItemVO itemVO=this.convertVOFromModel(itemModel);
             return itemVO;
        }).collect(Collectors.toList());

        return  CommonReturnType.create(itemModelList);
    }

    @GetMapping("/get")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam("id") Integer id) throws BussinessException {
        String itemKey=CacheConstant.ITEM_CACHE_PREFIX+ id;
        ItemModel itemModel=null;

        itemModel= (ItemModel) cacheUtil.getFromCommonCache(itemKey);
        if(itemModel==null){
            itemModel=redisUtil.getCacheObject(itemKey);
            if(itemModel==null){
                itemModel=itemService.getItem(id);
                redisUtil.setCacheObjectExpire(itemKey,itemModel,10, TimeUnit.MINUTES);
            }
            cacheUtil.setCommonCache(itemKey,itemModel);
        }

        ItemVO itemVO = this.convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @PostMapping("/publishPromo")
    @ResponseBody
    public CommonReturnType publishPromo(@RequestParam("promoId") Integer id) throws BussinessException {
        promoService.publishPromoById(id);
        return CommonReturnType.create(null);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);

        PromoModel promoModel=itemModel.getPromoModel();
        if(promoModel!=null){
            itemVO.setPromoId(promoModel.getId());
            itemVO.setPromoPrice(promoModel.getPromoPrice());
            itemVO.setPromoStatus(promoModel.getStatus());
            itemVO.setStartTime(promoModel.getStartTime());
        }else{
            itemVO.setPromoStatus(0);
            itemVO.setPromoId(0);
        }
        return itemVO;
    }

}
