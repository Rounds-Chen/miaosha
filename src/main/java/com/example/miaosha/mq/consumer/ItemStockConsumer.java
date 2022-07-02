package com.example.miaosha.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.mq.MqConstant;
import com.example.miaosha.mq.message.ItemStockMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RabbitListener(queues = MqConstant.ITEM_STOCK_QUEUE)
public class ItemStockConsumer {
    @Autowired
    ItemStockDtoMapper itemStockDtoMapper;

    @RabbitHandler(isDefault = true)
    public void asyncReceive(String message){
        System.out.println("收到 message: "+message);
        ItemStockMessage itemStockMessage= JSON.toJavaObject(JSON.parseObject(message),ItemStockMessage.class);

        Integer itemId=itemStockMessage.getItemId();
        Integer amount=itemStockMessage.getAmount();

        itemStockDtoMapper.decreaseStock(itemId,amount);
    }
}
