/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 22:35
 * @since 1.6.1
 */
public final class StringMap {
    /** Map */
    private final Map<String, Object> map;

    /**
     * String map
     *
     * @since 1.6.1
     */
    public StringMap() {
        this(new HashMap<String, Object>());
    }

    /**
     * String map
     *
     * @param map map
     * @since 1.6.1
     */
    public StringMap(Map<String, Object> map) {
        this.map = map;
    }

    /**
     * Put
     *
     * @param key   key
     * @param value value
     * @return the string map
     * @since 1.6.1
     */
    public StringMap put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    /**
     * Put not empty
     *
     * @param key   key
     * @param value value
     * @return the string map
     * @since 1.6.1
     */
    public StringMap putNotEmpty(String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            this.map.put(key, value);
        }
        return this;
    }

    /**
     * Put not null
     *
     * @param key   key
     * @param value value
     * @return the string map
     * @since 1.6.1
     */
    public StringMap putNotNull(String key, Object value) {
        if (value != null) {
            this.map.put(key, value);
        }
        return this;
    }


    /**
     * Put when
     *
     * @param key  key
     * @param val  val
     * @param when when
     * @return the string map
     * @since 1.6.1
     */
    public StringMap putWhen(String key, Object val, boolean when) {
        if (when) {
            this.map.put(key, val);
        }
        return this;
    }

    /**
     * Put all
     *
     * @param map map
     * @return the string map
     * @since 1.6.1
     */
    public StringMap putAll(Map<String, Object> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * Put all
     *
     * @param map map
     * @return the string map
     * @since 1.6.1
     */
    public StringMap putAll(StringMap map) {
        this.map.putAll(map.map);
        return this;
    }

    /**
     * For each
     *
     * @param imp imp
     * @since 1.6.1
     */
    public void forEach(Consumer imp) {
        for (Map.Entry<String, Object> i : this.map.entrySet()) {
            imp.accept(i.getKey(), i.getValue());
        }
    }

    /**
     * Size
     *
     * @return the int
     * @since 1.6.1
     */
    public int size() {
        return this.map.size();
    }

    /**
     * Map
     *
     * @return the map
     * @since 1.6.1
     */
    public Map<String, Object> map() {
        return this.map;
    }

    /**
     * Get
     *
     * @param key key
     * @return the object
     * @since 1.6.1
     */
    public Object get(String key) {
        return this.map.get(key);
    }

    /**
     * Key set
     *
     * @return the set
     * @since 1.6.1
     */
    public Set<String> keySet() {
        return this.map.keySet();
    }

    /**
     * Form string
     *
     * @return the string
     * @since 1.6.1
     */
    public String formString() {
        StringBuilder b = new StringBuilder();
        this.forEach(new Consumer() {
            private boolean notStart = false;

            @Override
            public void accept(String key, Object value) {
                if (this.notStart) {
                    b.append("&");
                }
                try {
                    b.append(URLEncoder.encode(key, "UTF-8")).append('=')
                        .append(URLEncoder.encode(value.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
                this.notStart = true;
            }
        });
        return b.toString();
    }

    /**
     * Json string
     *
     * @return the string
     * @since 1.6.1
     */
    public String jsonString() {
        return Json.encode(this);
    }

    /**
     * <p>Company: 成都返空汇网络技术有限公司 </p>
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@fkhwl.com"
     * @date 2021.02.18 22:35
     * @since 1.6.1
     */
    public interface Consumer {
        /**
         * Accept
         *
         * @param key   key
         * @param value value
         * @since 1.6.1
         */
        void accept(String key, Object value);
    }
}
