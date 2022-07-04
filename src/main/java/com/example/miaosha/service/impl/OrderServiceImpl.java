package com.example.miaosha.service.impl;

import com.example.miaosha.dao.OrderDtoMapper;
import com.example.miaosha.dao.OrderLogDtoMapper;
import com.example.miaosha.dao.SequenceDtoMapper;
import com.example.miaosha.dto.OrderDto;
import com.example.miaosha.dto.OrderLogDto;
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
import java.util.UUID;

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

    @Autowired
    OrderLogDtoMapper orderLogDtoMapper;


    @Override
    @Transactional
    public OrderModel create(Integer userId, Integer itemId, Integer amount, Integer promoId) throws BussinessException {
        // amout参数校验
        if (amount <= 0 || amount > 99) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "數量信息不存在");
        }

        // redis缓存中减库存
        boolean result = itemService.decreaseStockInCache(itemId, amount);
        if (!result) {
            // redis缓存恢复
            itemService.decreaseStockInCache(itemId, -1 * amount);
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品庫存不足");
        }

        // 订单入库
        ItemModel itemModel=itemService.getItemInCache(itemId);
        OrderModel orderModel = new OrderModel();
        orderModel.setItemPrice(itemModel.getPromoModel().getPromoPrice());
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setOrderPrice(BigDecimal.valueOf(amount).multiply(orderModel.getItemPrice()));
        orderModel.setPromoPrice(orderModel.getItemPrice());
        orderModel.setPromoId(promoId);
        // 生成訂單交易號
        orderModel.setId(generateOrderNo());
        orderDtoMapper.insertSelective(this.convertFromOrderModel(orderModel));

        // 4. 商品销量增加
        itemService.increaseSales(itemId, amount);

        return orderModel;
    }

    @Override
    public OrderModel createByTransication(Integer userId, Integer itemId, Integer amout, Integer promoId) throws BussinessException {
        // 创建订单
        OrderModel orderModel=this.create(userId,itemId,amout,promoId);

        // 记录本地创建订单记录
        String logId=saveLocalOrderMsg(itemId,amout);

        // 发送消息到mq
        if(!itemStockProducer.syncSend(logId,itemId,amout)){
            itemService.decreaseStockInCache(itemId, -1 * amout);
        };

        return orderModel;
    }

    // 记录订单创建消息到本地 此时log的status默认0--未被确认收到
    private String saveLocalOrderMsg(Integer itemId,Integer amount){
        String logId= UUID.randomUUID().toString().replace("-","");
        OrderLogDto orderLogDto=new OrderLogDto();
        orderLogDto.setOrderLogId(logId);
        orderLogDto.setItemId(itemId);
        orderLogDto.setAmount(amount);

        orderLogDtoMapper.insertSelective(orderLogDto);
        return logId;
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
