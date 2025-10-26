package info.dong4j.idea.plugin.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * StringMap 是一个用于封装 Map<String, Object> 的记录类，提供了一组便捷的方法来操作字符串键值对。
 * <p>
 * 该类主要用于简化对 Map 的操作，支持常见的 put、putAll、get、forEach 等方法，并提供了将 Map 转换为 URL 查询字符串和 JSON 字符串的功能。
 * <p>
 * 该类还定义了一个 Consumer 接口，用于遍历 Map 中的键值对，适用于需要对每个键值对进行处理的场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.1
 */
public record StringMap(Map<String, Object> map) {
    /**
     * 初始化一个空的 StringMap 实例
     * <p>
     * 使用默认的 HashMap 实现来初始化 StringMap
     *
     * @since 1.6.1
     */
    public StringMap() {
        this(new HashMap<String, Object>());
    }

    /**
     * String map
     * <p>
     * 该类用于表示字符串映射结构，提供对键值对的存储和操作功能。
     *
     * @param map map
     * @since 1.6.1
     */
    public StringMap {
    }

    /**
     * 向Map中添加键值对并返回当前对象
     * <p>
     * 该方法将指定的键值对存入Map中，并返回当前StringMap实例，支持链式调用
     *
     * @param key   要存储的键
     * @param value 要存储的值
     * @return 当前StringMap实例，支持链式调用
     * @since 1.6.1
     */
    public StringMap put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    /**
     * 向字符串映射中添加非空值的键值对
     * <p>
     * 如果提供的值不为空，则将其与指定的键一起添加到映射中。
     *
     * @param key   要添加的键
     * @param value 要添加的值，仅当不为空时才会被添加
     * @return 返回当前的字符串映射对象，支持链式调用
     * @since 1.6.1
     */
    public StringMap putNotEmpty(String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            this.map.put(key, value);
        }
        return this;
    }

    /**
     * 将指定的键值对以非空方式放入字符串映射中
     * <p>
     * 如果值不为 null，则将键值对添加到映射中，否则忽略该操作。
     *
     * @param key   要放入映射中的键
     * @param value 要放入映射中的值，若为 null 则不进行添加
     * @return 返回当前字符串映射对象，支持链式调用
     * @since 1.6.1
     */
    public StringMap putNotNull(String key, Object value) {
        if (value != null) {
            this.map.put(key, value);
        }
        return this;
    }

    /**
     * 根据条件将键值对放入字符串映射中
     * <p>
     * 如果条件为真，则将指定的键和值放入映射中，否则不进行操作。该方法返回当前对象，支持链式调用。
     *
     * @param key  要放入映射中的键
     * @param val  要放入映射中的值
     * @param when 条件判断参数，为true时放入映射，为false时不放入
     * @return 当前对象，支持链式调用
     * @since 1.6.1
     */
    public StringMap putWhen(String key, Object val, boolean when) {
        if (when) {
            this.map.put(key, val);
        }
        return this;
    }

    /**
     * 将指定的 Map 数据合并到当前 StringMap 中
     * <p>
     * 该方法将传入的 Map 中的所有键值对合并到当前 StringMap 实例中，并返回当前实例以便链式调用
     *
     * @param map 要合并的 Map 对象，键为 String 类型，值为 Object 类型
     * @return 当前 StringMap 实例，支持链式调用
     * @since 1.6.1
     */
    public StringMap putAll(Map<String, Object> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * 将指定的 StringMap 中的所有条目合并到当前 StringMap 中
     * <p>
     * 该方法将传入的 StringMap 中的所有键值对添加到当前实例的 map 中，并返回当前实例以支持链式调用
     *
     * @param map 要合并的 StringMap 对象
     * @return 当前 StringMap 实例，支持链式调用
     */
    public StringMap putAll(StringMap map) {
        this.map.putAll(map.map);
        return this;
    }

    /**
     * 对映射表中的每个条目执行指定的操作
     * <p>
     * 遍历当前映射表的所有键值对，并对每个条目调用传入的 Consumer 接口的 accept 方法
     *
     * @param imp 要执行的操作，实现 Consumer 接口
     * @since 1.6.1
     */
    public void forEach(Consumer imp) {
        for (Map.Entry<String, Object> i : this.map.entrySet()) {
            imp.accept(i.getKey(), i.getValue());
        }
    }

    /**
     * 获取集合中的元素数量
     * <p>
     * 返回当前集合中元素的个数
     *
     * @return 集合中元素的数量
     */
    public int size() {
        return this.map.size();
    }

    /**
     * 返回当前对象的映射表示形式
     * <p>
     * 将对象的属性转换为一个字符串键和对象值的映射结构
     *
     * @return 当前对象的映射表示
     * @since 1.6.1
     */
    @Override
    public Map<String, Object> map() {
        return this.map;
    }

    /**
     * 根据指定的键获取对应的对象
     * <p>
     * 通过键从映射中查找并返回对应的值对象
     *
     * @param key 键，用于查找对应的值
     * @return 对应的值对象，若键不存在则返回 null
     * @since 1.6.1
     */
    public Object get(String key) {
        return this.map.get(key);
    }

    /**
     * 获取键集合
     * <p>
     * 返回此映射中包含的键的集合视图。
     *
     * @return 键的集合
     * @since 1.6.1
     */
    public Set<String> keySet() {
        return this.map.keySet();
    }

    /**
     * 将当前对象转换为格式化的字符串，用于构建查询参数或表单数据
     * <p>
     * 遍历当前对象的所有键值对，使用URL编码格式拼接成字符串，格式为 "key=value"，多个键值对之间用 "&" 分隔。
     *
     * @return 格式化后的字符串
     * @since 1.6.1
     */
    public String formString() {
        StringBuilder b = new StringBuilder();
        this.forEach(new Consumer() {
            /** 是否已启动标志，false 表示未启动，true 表示已启动 */
            private boolean notStart = false;

            /**
             * 处理键值对，用于构建URL编码的查询字符串
             * <p>
             * 该方法在遍历键值对时，将每个键值对以"key=value"的形式追加到字符串构建器中，并在第一个键值对前添加"&"符号。
             *
             * @param key   要编码的键
             * @param value 要编码的值
             */
            @Override
            public void accept(String key, Object value) {
                if (this.notStart) {
                    b.append("&");
                }
                b.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
                this.notStart = true;
            }
        });
        return b.toString();
    }

    /**
     * 返回当前对象的 JSON 格式字符串表示
     * <p>
     * 将当前对象转换为 JSON 格式的字符串，可用于序列化或传输数据
     *
     * @return 当前对象的 JSON 字符串表示
     * @since 1.6.1
     */
    public String jsonString() {
        return Json.encode(this);
    }

    /**
     * 消费者接口
     * <p>
     * 用于定义接受键值对数据的回调接口，通常用于数据处理或事件监听场景。
     * 实现该接口的类需要提供一个 accept 方法，用于处理传入的 key 和 value。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2021.02.18
     * @since 1.6.1
     */
    public interface Consumer {
        /**
         * 接收键值对数据
         * <p>
         * 用于处理传入的键和对应的值，具体逻辑由实现类定义
         *
         * @param key   要处理的键
         * @param value 与键对应的值
         * @since 1.6.1
         */
        void accept(String key, Object value);
    }
}
