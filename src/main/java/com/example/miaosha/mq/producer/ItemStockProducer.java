package com.example.miaosha.mq.producer;

import com.alibaba.fastjson.JSON;
import com.example.miaosha.dao.OrderLogDtoMapper;
import com.example.miaosha.dto.OrderLogDto;
import com.example.miaosha.mq.MqConstant;
import com.example.miaosha.mq.message.ItemStockMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class ItemStockProducer {
    @Resource
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderLogDtoMapper orderLogDtoMapper;

    @PostConstruct
    public void setup(){
        // 消息发送完成后，则回调此方法，ack代表此方法是否发送成功
        // 设置定时轮询 重发超过一段时间还未ack的消息
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback(){
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //ack为true,代表MQ已经准确收到消息
                if(ack){
                    OrderLogDto orderLogDto=orderLogDtoMapper.selectByPrimaryKey(correlationData.getId());
                    orderLogDto.setStatus(1);// 标志消息发送成功
                    orderLogDtoMapper.updateByPrimaryKeySelective(orderLogDto);
                }
            }
        });
    }

    public boolean syncSend(String logId,Integer itemId,Integer amount){
        ItemStockMessage message=new ItemStockMessage();
        message.setItemId(itemId);
        message.setAmount(amount);
        message.setMsgId(logId);
        try {
            rabbitTemplate.convertAndSend(MqConstant.ORDER_EXCHANGE, MqConstant.ITEM_STOCK_ROUTE_KEY, JSON.toJSONString(message),new CorrelationData(logId));
            System.out.println("发送：item:"+itemId+" stockDec:"+amount);
        }catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }
}
