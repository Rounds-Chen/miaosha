package com.example.miaosha.service.impl;

import com.example.miaosha.dao.OrderDtoMapper;
import com.example.miaosha.dao.SequenceDtoMapper;
import com.example.miaosha.dto.OrderDto;
import com.example.miaosha.dto.SequenceDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.mq.producer.ItemStockProducer;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.OrderService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.UserService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.service.model.OrderModel;
import com.example.miaosha.service.model.PromoModel;
import com.example.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    @Autowired
    SequenceDtoMapper sequenceDtoMapper;

    @Autowired
    OrderDtoMapper orderDtoMapper;

    @Autowired
    PromoService promoService;

    @Resource
    ItemStockProducer itemStockProducer;


    @Override
    @Transactional
    public OrderModel create(Integer userId, Integer itemId, Integer amount, Integer promoId) throws BussinessException {
        // 1. 校验下单参数
        // a. 用户商品信息校验
        UserModel userModel = userService.getUserInCacheById(userId);
        if (userModel == null) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
        }
        ItemModel itemModel = itemService.getItemInCache(itemId);
        if (itemModel == null) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "數量信息不存在");
        }
        // b.活动信息校验
        PromoModel promoModel=itemModel.getPromoModel();
        if(promoModel==null||promoId!=promoModel.getId()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动不存在");
        }else if(promoModel.getStatus()!=2){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动未开始");
        }

        // 2. 商品库存减（落单即减）
        // a. redis缓存中减库存
        boolean result = itemService.decreaseStockInCache(itemId, amount);
        if (!result) {
            // redis缓存恢复
            itemService.decreaseStockInCache(itemId,-1*amount);
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品庫存不足");
        }
        // b. 放入异步消息队列
        if(!itemStockProducer.syncSend(itemId,amount)){
            itemService.decreaseStockInCache(itemId,-1*amount);
        }


        // 3. 订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setItemPrice(promoModel.getPromoPrice());
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setOrderPrice(BigDecimal.valueOf(amount).multiply(orderModel.getItemPrice()));
        // 生成訂單交易號
        orderModel.setId(generateOrderNo());

        orderDtoMapper.insertSelective(this.convertFromOrderModel(orderModel));

        // 4. 商品销量增加
        itemService.increaseSales(itemId, amount);
        return orderModel;
    }

    // 獲取訂單號
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {
        // 訂單號有16位
        StringBuilder sb = new StringBuilder();
        // 前8位為日期
        LocalDateTime now = LocalDateTime.now();
        String nowTime = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowTime);

        // 中間6位為自增序列
        SequenceDto sequenceDto = sequenceDtoMapper.selectByPrimaryKey("order_info");
        String curVal = String.valueOf(sequenceDto.getCurrentValue());
        sequenceDto.setCurrentValue(sequenceDto.getCurrentValue() + sequenceDto.getStep());
        sequenceDtoMapper.updateByPrimaryKeySelective(sequenceDto);

        for (int i = 0; i < Math.max(0, 6 - curVal.length()); i++)
            sb.append("0");
        sb.append(curVal);

        // 最後兩位為分庫分表位 暫時00
        sb.append("00");

        return sb.toString();
    }

    private OrderDto convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDto orderDO = new OrderDto();
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }
}
