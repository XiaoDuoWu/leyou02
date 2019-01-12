package com.leyou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-04-24 17:20
 **/
public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }

    public static <T> T toBean(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <E> List<E> toList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static void main(String[] args) {
        // 序列化
        String json = toString(Arrays.asList(new User("Rose", 18), new User("Jack", 21)));
        System.out.println("json = " + json);// [{"name":"Rose","age":18},{"name":"Jack","age":21}]

        // 反序列化
        json = "{\"name\":\"Rose\",\"age\":18}";
        User user = toBean(json, User.class);
        System.out.println("user = " + user);

        // list
        json = "[{\"name\":\"Rose\",\"age\":18},{\"name\":\"Jack\",\"age\":21}]";
        List<User> users = toList(json, User.class);
        System.out.println("users = " + users);

        // toMap
        json = "{\"name\":\"Rose\",\"age\":18}";
        Map<String, Object> map = toMap(json, String.class, Object.class);
        System.out.println("map = " + map);

        // 变态
        json = "{\"heima52\": [{\"name\":\"Rose\",\"age\":18},{\"name\":\"Jack\",\"age\":21}],\"heima54\": [{\"name\":\"Rose\",\"age\":18},{\"name\":\"Jack\",\"age\":21}]}";
        Map<String, List<User>> stringListMap = nativeRead(json, new TypeReference<Map<String, List<User>>>() {});
        System.out.println("stringListMap = " + stringListMap);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User{
        String name;
        int age;
    }
}
