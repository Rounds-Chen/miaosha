package com.example.miaosha.service;

import com.example.miaosha.error.BussinessException;
import com.example.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    // 新增商品
     ItemModel createItem(ItemModel item) throws BussinessException;

     // 商品列表浏览
    List<ItemModel> itemList();

    // 商品详情浏览
    ItemModel getItem(Integer id) throws BussinessException;

    // 商品減庫存
    boolean decreaseStock(Integer itemId,Integer amount);

    // 商品銷量增加
    void increaseSales(Integer itemId,Integer amount);

}
