package com.phone.uin.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;


/**
 * 封装Json库
 */
public class JsonUtil {

    private static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final Feature[] deserializerFeature = {
            Feature.AutoCloseSource,
            Feature.InternFieldNames,
            Feature.UseBigDecimal,
            Feature.AllowUnQuotedFieldNames,
            Feature.AllowSingleQuotes,
            Feature.AllowArbitraryCommas,
            Feature.SortFeidFastMatch,
            Feature.IgnoreNotMatch //FastJson默认值
    };

    private static final SerializerFeature[] serializerFeature = {
            SerializerFeature.QuoteFieldNames,
            SerializerFeature.SkipTransientField,
            SerializerFeature.WriteEnumUsingToString,
            SerializerFeature.SortField, //FastJson默认值
            SerializerFeature.WriteMapNullValue, // 输出空置字段
            SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
            SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
            SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
            SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null                                                   };
    };


    /**
     * 将json字符串解析为对象
     */
    public static final <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz, new Feature[0]);

    }

    /**
     * 将json字符串解析为对象列表
     */
    public static final <T> List<T> parseObjectList(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }


    /**
     * 将对象解析为字符串
     */
    public static final String toJSONString(Object object) {
        return JSON.toJSONStringWithDateFormat(object, DEFFAULT_DATE_FORMAT, serializerFeature);
    }

    /**
     * 获得json字符串中元素
     */
    public static final String getElementFromJson(String json, String element) {
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString(element);
    }


}
