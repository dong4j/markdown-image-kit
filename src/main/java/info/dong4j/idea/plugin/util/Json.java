package info.dong4j.idea.plugin.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * JSON 工具类
 * <p>
 * 提供 JSON 数据的编码和解码功能，支持将对象转换为 JSON 字符串，以及将 JSON 字符串或 JSON 元素转换为 Java 对象。
 * 包含对 Map 和 StringMap 类型的处理，适用于通用的 JSON 数据操作场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.1
 */
public final class Json {
    /**
     * 私有构造函数，用于防止外部实例化
     * <p>
     * 该构造函数为私有，确保 Json 类无法被外部直接实例化
     *
     * @since 1.6.1
     */
    private Json() {
    }

    /**
     * 将给定的 StringMap 对象转换为 JSON 格式的字符串
     * <p>
     * 该方法使用 Gson 库将 StringMap 内部的 Map 结构序列化为 JSON 字符串
     *
     * @param map 需要转换的 StringMap 对象
     * @return 转换后的 JSON 字符串
     * @since 1.6.1
     */
    public static String encode(StringMap map) {
        return new Gson().toJson(map.map());
    }

    /**
     * 对指定对象进行编码转换，返回其 JSON 格式的字符串表示
     * <p>
     * 使用 Gson 库将对象序列化为 JSON 字符串，支持 null 值的序列化
     *
     * @param obj 要编码的对象
     * @return 对象的 JSON 字符串表示
     * @since 1.6.1
     */
    public static String encode(Object obj) {
        return new GsonBuilder().serializeNulls().create().toJson(obj);
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     * <p>
     * 使用 Gson 库将传入的 JSON 字符串转换为指定类类型的对象
     *
     * @param json     需要反序列化的 JSON 字符串
     * @param classOfT 目标对象的类类型
     * @return 反序列化后的对象
     * @since 1.6.1
     */
    public static <T> T decode(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    /**
     * 将 JSON 元素反序列化为指定类型的对象
     * <p>
     * 使用 Gson 库将传入的 JsonElement 对象转换为指定类类型的实例
     *
     * @param <T>         通用类型参数，表示目标对象的类型
     * @param jsonElement 需要反序列化的 JSON 元素
     * @param clazz       目标对象的类类型
     * @return 反序列化后的对象实例
     * @since 1.6.1
     */
    public static <T> T decode(JsonElement jsonElement, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(jsonElement, clazz);
    }

    /**
     * 将 JSON 字符串解码为 StringMap 对象
     * <p>
     * 使用 Gson 库将传入的 JSON 字符串转换为 Map 类型，再封装为 StringMap 返回
     *
     * @param json 要解码的 JSON 字符串
     * @return 转换后的 StringMap 对象
     */
    public static StringMap decode(String json) {
        // CHECKSTYLE:OFF
        Type t = new TypeToken<Map<String, Object>>() {
        }.getType();
        // CHECKSTYLE:ON
        Map<String, Object> x = new Gson().fromJson(json, t);
        return new StringMap(x);
    }
}
