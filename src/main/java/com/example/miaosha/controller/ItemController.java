package com.example.miaosha.controller;

import com.example.miaosha.controller.viewobject.ItemVO;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.response.CommonReturnType;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.service.model.PromoModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/item")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class ItemController extends BaseController{

    @Autowired
    ItemService itemService;

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

        return  CommonReturnType.create(list);
    }

    @GetMapping("/get")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam("id") Integer id) throws BussinessException {
        ItemModel itemModel=itemService.getItem(id);
        ItemVO itemVO=this.convertVOFromModel(itemModel);

        return CommonReturnType.create(itemVO);
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
        }
        return itemVO;
    }

}
