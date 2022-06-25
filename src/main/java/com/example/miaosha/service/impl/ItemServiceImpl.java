package com.example.miaosha.service.impl;

import com.example.miaosha.dao.ItemDtoMapper;
import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.dto.ItemDto;
import com.example.miaosha.dto.ItemStockDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.validation.ValidationResult;
import com.example.miaosha.validation.Validatorer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    Validatorer validatorer;

    @Autowired
    ItemDtoMapper itemDtoMapper;

    @Autowired
    ItemStockDtoMapper itemStockDtoMapper;

    @Override
    public ItemModel createItem(ItemModel item) throws BussinessException {
        ValidationResult result=validatorer.validate(item);
        if(result.isHasError()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsgs());
        }

        ItemDto itemDto=this.convertItemDtoFromItemModel(item);
        itemDtoMapper.insertSelective(itemDto);

        ItemStockDto itemStockDto=this.convertItemStockDtoFromItemModel(item);
        itemStockDtoMapper.insertSelective(itemStockDto);

        return item;
    }

    @Override
    public List<ItemModel> itemList() {
        List<ItemDto> list=itemDtoMapper.itemList();

        List<ItemModel> itemModelList=list.stream().map(itemDto -> {
            ItemStockDto itemStockDto=itemStockDtoMapper.selectByItemId(itemDto.getId());
            ItemModel itemModel=this.convertModelFromDataObject(itemDto,itemStockDto);

            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItem(Integer id) throws BussinessException {
        ItemDto itemDto=itemDtoMapper.selectByPrimaryKey(id);
        if(itemDto==null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"商品不存在");
        }

        ItemStockDto itemStockDto=itemStockDtoMapper.selectByItemId(itemDto.getId());
        if(itemStockDto==null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"商品不存在");

        }
        ItemModel itemModel=this.convertModelFromDataObject(itemDto,itemStockDto);

        return itemModel;
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