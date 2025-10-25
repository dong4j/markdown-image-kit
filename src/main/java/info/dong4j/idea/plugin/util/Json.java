package info.dong4j.idea.plugin.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 22:35
 * @since 1.6.1
 */
public final class Json {
    /**
     * Json
     *
     * @since 1.6.1
     */
    private Json() {
    }

    /**
     * Encode
     *
     * @param map map
     * @return the string
     * @since 1.6.1
     */
    public static String encode(StringMap map) {
        return new Gson().toJson(map.map());
    }

    /**
     * Encode
     *
     * @param obj obj
     * @return the string
     * @since 1.6.1
     */
    public static String encode(Object obj) {
        return new GsonBuilder().serializeNulls().create().toJson(obj);
    }

    /**
     * Decode
     *
     * @param <T>      parameter
     * @param json     json
     * @param classOfT class of t
     * @return the t
     * @since 1.6.1
     */
    public static <T> T decode(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    /**
     * Decode
     *
     * @param <T>         parameter
     * @param jsonElement json element
     * @param clazz       clazz
     * @return the t
     * @since 1.6.1
     */
    public static <T> T decode(JsonElement jsonElement, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(jsonElement, clazz);
    }

    /**
     * Decode
     *
     * @param json json
     * @return the string map
     * @since 1.6.1
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
