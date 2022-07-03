package com.example.miaosha.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.example.miaosha.dao.ItemStockDtoMapper;
import com.example.miaosha.mq.MqConstant;
import com.example.miaosha.mq.message.ItemStockMessage;
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

    @RabbitHandler(isDefault = true)
    public void asyncReceive(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // TODO 消费消息的幂等性保证
            System.out.println("收到 message: " + message);
            ItemStockMessage itemStockMessage = JSON.toJavaObject(JSON.parseObject(message), ItemStockMessage.class);

            Integer itemId = itemStockMessage.getItemId();
            Integer amount = itemStockMessage.getAmount();
            itemStockDtoMapper.decreaseStock(itemId, amount);

            channel.basicAck(tag,false);
        }catch(Exception e){
            // TODO rabbitmq没有重发次数限制 可以用redis控制 超过一定次数后人工干预 死信队列
            // https://juejin.cn/post/6979390382371143694#heading-12
            // b1: requeue
            channel.basicNack(tag,false,true);
        }
    }
}
