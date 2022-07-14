package com.example.miaosha.util;

public interface CacheConstant {
    // 用户注册验证码前缀
    String TELEPHONE_OPT_CODE_PREFIX="OPT_";

    // 商品缓存前缀
    String ITEM_CACHE_PREFIX="ITEM_";

    // 用户信息缓存前缀
    String USER_CACHE_PREFIX="USER_";

    // 订单商品信息缓存前缀
    String ORDER_ITEM_CACHE_PREFIX="ORDER_ITEM_";

    // 商品库存信息缓存前缀
    String ITEM_STOCK_CACHE_PREFIX="ITEM_STOCK_";

    // 商品销量信息缓存前缀
    String ITEM_SALES_CACHE_PREFIX="ITEM_SALES_";

    // 已消费库存消息set key
    String CONSUMED_STOCK_DECREASE_MSG="CONSUMED_STOCK_DECREASE_MSG";

    // 已消费销量消息set key
    String CONSUMED_SALES_INCREASE_MSG="CONSUMED_SALES_INCREASE_MSG";

    // 秒杀活动商品库存限额
    String PROMO_DOOR_PREFIX="PROMO_DOOR_";

    // promo token
    String PROMO_TOKEN_PREFIX="PROMO_TOKEN_";

    // 验证图片码缓存前缀
    String VERIFY_CODE_PREFIX="VERIFY_CODE_";

    // order_info自增序列当前值
    String ORDER_INFO_CUR_VALUE="ORDER_INFO_CUR_VALUE";


}
