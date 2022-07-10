//package com.example.miaosha.task;
//
//import com.example.miaosha.dao.SequenceDtoMapper;
//import com.example.miaosha.dto.OrderLogDto;
//import com.example.miaosha.dto.SequenceDto;
//import com.example.miaosha.util.CacheConstant;
//import com.example.miaosha.util.RedisUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.Date;
//import java.util.List;
//
//@Component
//public class SequenceNoScheduleTask {
//    @Autowired
//    SequenceDtoMapper sequenceDtoMapper;
//
//    @Resource
//    RedisUtil redisUtil;
//
//    // 将新sequence写入mysql
//    // 每1min执行一次
//    @Scheduled(cron="0 * * * * *")
//    public void writeSequenceNo(){
//       SequenceDto sequenceDto=sequenceDtoMapper.selectByPrimaryKey("order_info");
//       sequenceDto.setCurrentValue(redisUtil.getCacheObject(CacheConstant.ORDER_INFO_CUR_VALUE));
//       sequenceDtoMapper.updateByPrimaryKeySelective(sequenceDto);
//        System.out.println("sequence +1--------------------");
//    }
//
//}
