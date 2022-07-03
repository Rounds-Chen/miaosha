package com.example.miaosha.task;

import com.example.miaosha.dao.OrderLogDtoMapper;
import com.example.miaosha.dto.OrderLogDto;
import com.example.miaosha.mq.producer.ItemStockProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
public class ProducerScheduleTask{
    @Autowired
    OrderLogDtoMapper orderLogDtoMapper;

    @Resource
    ItemStockProducer itemStockProducer;

    // 设置超时5min未更新的消息重新发送
    // 每两小时执行一次
    @Scheduled(cron="0 */2 * * *")
    public void retrySendMsg(){
        List<OrderLogDto> res=orderLogDtoMapper.selectDumpedLog(new Date(),5);
        for(OrderLogDto orderLogDto : res){
            itemStockProducer.syncSend(orderLogDto.getOrderLogId(),orderLogDto.getItemId(),orderLogDto.getAmount());
        }
    }
}
