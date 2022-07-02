package com.example.miaosha.config;

import com.example.miaosha.mq.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    public static class DirectExchangeDemoConfiguration{
        // 创建itemstock写回mysql的queue
        @Bean
        public Queue itemStockQueue(){
            return new Queue(MqConstant.ITEM_STOCK_QUEUE,true);
        }

        // 创建Order相关exchange
        @Bean
        public DirectExchange orderExchange(){
            return new DirectExchange(MqConstant.ORDER_EXCHANGE);
        }

        // 将上述que和exc绑定
        @Bean
        public Binding stockOrderBind(){
            return BindingBuilder.bind(itemStockQueue())
                    .to(orderExchange())
                    .with(MqConstant.ITEM_STOCK_ROUTE_KEY);
        }

    }
}
