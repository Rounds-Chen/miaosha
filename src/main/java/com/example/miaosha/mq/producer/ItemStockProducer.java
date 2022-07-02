package com.example.miaosha.mq.producer;

import com.alibaba.fastjson.JSON;
import com.example.miaosha.mq.MqConstant;
import com.example.miaosha.mq.message.ItemStockMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;

@Component
public class ItemStockProducer {
    @Resource
    RabbitTemplate rabbitTemplate;

    public boolean syncSend(Integer itemId,Integer amount){
        ItemStockMessage message=new ItemStockMessage();
        message.setItemId(itemId);
        message.setAmount(amount);
        try {
            rabbitTemplate.convertAndSend(MqConstant.ORDER_EXCHANGE, MqConstant.ITEM_STOCK_ROUTE_KEY, JSON.toJSONString(message));
            System.out.println("发送：item:"+itemId+" stockDec:"+amount);
        }catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }
}
