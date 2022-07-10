package com.example.miaosha.task;

import com.example.miaosha.dao.OrderLogDtoMapper;
import com.example.miaosha.dto.OrderLogDto;
import com.example.miaosha.mq.producer.ItemProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//@Component
//public class ProducerScheduleTask{
//    @Autowired
//    OrderLogDtoMapper orderLogDtoMapper;
//
//    @Resource
//    ItemProducer itemStockProducer;
//
//    // 设置超时5min未更新的消息重新发送
//    // 每两小时执行一次
////    @Scheduled(cron="* */1 * * * *")
//    public void retrySendMsg(){
//        List<OrderLogDto> res=orderLogDtoMapper.selectDumpedLog(new Date(),5);
//        for(OrderLogDto orderLogDto : res){
//            orderLogDto.setStatus(1);
//            orderLogDtoMapper.updateByPrimaryKeySelective(orderLogDto);
//
//            System.out.println(orderLogDto.getItemId());
//
//            String logId = UUID.randomUUID().toString().replace("-", "");
//            itemStockProducer.syncSend(logId,orderLogDto.getItemId(),orderLogDto.getAmount());
//        }
//    }
//}
