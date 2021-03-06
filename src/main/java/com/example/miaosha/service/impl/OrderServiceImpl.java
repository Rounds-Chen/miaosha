package com.example.miaosha.service.impl;

import com.example.miaosha.dao.OrderDtoMapper;
import com.example.miaosha.dao.OrderLogDtoMapper;
import com.example.miaosha.dao.SequenceDtoMapper;
import com.example.miaosha.dto.OrderDto;
import com.example.miaosha.dto.OrderLogDto;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.mq.producer.ItemProducer;
import com.example.miaosha.service.ItemService;
import com.example.miaosha.service.OrderService;
import com.example.miaosha.service.PromoService;
import com.example.miaosha.service.UserService;
import com.example.miaosha.service.model.ItemModel;
import com.example.miaosha.service.model.OrderModel;
import com.example.miaosha.util.CacheConstant;
import com.example.miaosha.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.*;

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
    ItemProducer itemStockProducer;

    @Resource
    RedisUtil redisUtil;

    @Autowired
    OrderLogDtoMapper orderLogDtoMapper;

    private ThreadPoolExecutor executor;

    @Value("${core.pool.size}")
    private int corePoolSize;
    @Value("${max.pool.size}")
    private int maxPoolSize;
    @Value("${queue.capacity}")
    private int queueCap;
    @Value("${keep.alive.time}")
    private long aliveTime;

    @PostConstruct
    void init() {
        executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                aliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCap),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }


    @Override
    @Transactional
    public OrderModel create(Integer userId, Integer itemId, Integer amount, Integer promoId) throws BussinessException {
        // amout????????????
        if (amount <= 0 || amount > 99) {
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "?????????????????????");
        }

        // redis??????????????????
        boolean result = itemService.decreaseStockInCache(itemId, amount);
        if (!result) {
            // redis????????????
            itemService.decreaseStockInCache(itemId, -1 * amount);
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "??????????????????");
        }

        // ????????????
        ItemModel itemModel = itemService.getItemInCache(itemId);
        OrderModel orderModel = new OrderModel();
        orderModel.setItemPrice(itemModel.getPromoModel().getPromoPrice());
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setOrderPrice(BigDecimal.valueOf(amount).multiply(orderModel.getItemPrice()));
        orderModel.setPromoPrice(orderModel.getItemPrice());
        orderModel.setPromoId(promoId);
        // ?????????????????????
        orderModel.setId(generateOrderNo());
        OrderDto orderDto=this.convertFromOrderModel(orderModel);
        orderDtoMapper.insertSelective(orderDto);


        // 4. ??????????????????
        itemService.increaseSalesInCache(itemId, amount);

        return orderModel;
    }

    @Override
    @Transactional
    public OrderModel createByTransication(Integer userId, Integer itemId, Integer amout, Integer promoId) throws BussinessException, ExecutionException, InterruptedException {
        // ????????????
        OrderModel orderModel = this.create(userId, itemId, amout, promoId);

        //???????????????20???????????????????????????????????????
        Future future=executor.submit(new Runnable() {
            @Override
            public void run() {
                // ??????????????????????????????
                String logId = saveLocalOrderMsg(itemId, amout);

                // ???????????????mq
                if (!itemStockProducer.syncSend(logId, itemId, amout)) {
                    itemService.decreaseStockInCache(itemId, -1 * amout);
                }
            }
        });
        try {
            future.get();
        }catch (Exception e){
            throw new BussinessException(EmBussinessError.UNKNOWN_ERROR,"?????????????????????");
        }
        String logId = saveLocalOrderMsg(itemId, amout);

        // ???????????????mq
        if (!itemStockProducer.syncSend(logId, itemId, amout)) {
            itemService.decreaseStockInCache(itemId, -1 * amout);
        }

        return orderModel;
    }

    // ????????????????????????????????? ??????log???status??????0--??????????????????
    private String saveLocalOrderMsg(Integer itemId, Integer amount) {
        String logId = UUID.randomUUID().toString().replace("-", "");
        OrderLogDto orderLogDto = new OrderLogDto();
        orderLogDto.setOrderLogId(logId);
        orderLogDto.setItemId(itemId);
        orderLogDto.setAmount(amount);

        orderLogDtoMapper.insertSelective(orderLogDto);
        return logId;
    }

    // ???????????????
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {
        // ????????????16???
        StringBuilder sb = new StringBuilder();
        // ???8????????????
        LocalDateTime now = LocalDateTime.now();
        String nowTime = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowTime);

        // ??????6??????????????????
//        SequenceDto sequenceDto = sequenceDtoMapper.selectByPrimaryKey("order_info");
//        String curVal = String.valueOf(sequenceDto.getCurrentValue());
//        sequenceDto.setCurrentValue(sequenceDto.getCurrentValue() + sequenceDto.getStep());
//        sequenceDtoMapper.updateByPrimaryKeySelective(sequenceDto);
        // redis?????????
        String  curVal=String.valueOf(redisUtil.incrementCacheObject(CacheConstant.ORDER_INFO_CUR_VALUE,1)-1);

        for (int i = 0; i < Math.max(0, 6 - curVal.length()); i++)
            sb.append("0");
        sb.append(curVal);

        // ?????????????????????????????? ??????00
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
