package com.example.miaosha.util;

public interface CacheConstant {
    // 商品缓存前缀
    String ITEM_CACHE_PREFIX="ITEM_";

    // 用户信息缓存前缀
    String USER_CACHE_PREFIX="USER_";

    // 订单商品信息缓存前缀
    String ORDER_ITEM_CACHE_PREFIX="ORDER_ITEM_";

    // 商品库存信息缓存前缀
    String ITEM_STOCK_CACHE_PREFIX="ITEM_STOCK_";

    // 已消费库存消息set key
    String CONSUMED_STOCK_DECREASE_MSG="CONSUMED_STOCK_DECREASE_MSG";

    // 秒杀活动商品库存限额
    String PROMO_DOOR_PREFIX="PROMO_DOOR_";

    // promo token
    String PROMO_TOKEN_PREFIX="PROMO_TOKEN_";

    // 验证图片码缓存前缀
    String VERIFY_CODE_PREFIX="VERIFY_CODE_";


}
