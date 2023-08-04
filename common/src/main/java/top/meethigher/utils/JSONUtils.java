package top.meethigher.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 基于fastjson的工具类
 *
 * @author chenchuancheng
 * @since 2023/5/21 10:25
 */
public class JSONUtils {


    /**
     * FastJSON使用中两个问题
     * 1. value为null的字段，自动省略
     * 2. key值默认不带引号
     * 写法过于麻烦，所以提出一个方法
     */
    public static String toJSONString(Object o) {
        return JSON.toJSONString(o, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames);
    }


}
