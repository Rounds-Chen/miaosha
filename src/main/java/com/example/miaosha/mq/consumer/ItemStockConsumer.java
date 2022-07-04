package com.example.miaosha.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.mq.MqConstant;
import com.example.miaosha.mq.message.ItemStockMessage;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@RabbitListener(queues = MqConstant.ITEM_STOCK_QUEUE)
public class ItemStockConsumer {
    @Autowired
    ItemStockDtoMapper itemStockDtoMapper;

    @Resource
    RedisUtil redisUtil;

    @RabbitHandler(isDefault = true)
    public void asyncReceive(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // 使用redis保证消费消息的幂等性
            // TODO 改成bitmap
            System.out.println("收到 message: " + message);
            ItemStockMessage itemStockMessage = JSON.toJavaObject(JSON.parseObject(message), ItemStockMessage.class);

            Integer itemId = itemStockMessage.getItemId();
            Integer amount = itemStockMessage.getAmount();
            String msgId=itemStockMessage.getMsgId();

            // 未消费过
            if(!redisUtil.isInCacheSet(CacheConstant.CONSUMED_STOCK_DECREASE_MSG,msgId)){
                itemStockDtoMapper.decreaseStock(itemId, amount);
                redisUtil.addInCacheSet(CacheConstant.CONSUMED_STOCK_DECREASE_MSG,msgId);
            }
            channel.basicAck(tag,false);
        }catch(Exception e){
            // TODO 死信队列
            // https://juejin.cn/post/6976778266472366087
            // b1: requeue
            channel.basicNack(tag,false,true);
        }
    }
}
