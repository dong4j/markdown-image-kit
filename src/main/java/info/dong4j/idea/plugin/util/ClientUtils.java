/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

import info.dong4j.idea.plugin.client.Client;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 加载 ClientUtils 时, 将通过反射创建 map 映射 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class ClientUtils {

    static {
        try {
            // 获取特定包下所有的类(包括接口和类, 排除内部类)
            // cache(ClassUtils.getAllClassByInterface(OssClient.class));
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    /**
     * 使用反射生成 client, 缓存 client 关系
     *
     * @param clsList the cls list
     * @since 0.0.1
     */
    private static void cache(List<String> clsList) {
        if (clsList != null && clsList.size() > 0) {
            for (String className : clsList) {
                Class<?> clz;
                try {
                    clz = Class.forName(className);
                    // 抽象类忽略
                    if (Modifier.isAbstract(clz.getModifiers())
                        || Modifier.isInterface(clz.getModifiers())
                        || !OssClient.class.isAssignableFrom(clz)) {
                        continue;
                    }
                } catch (ClassNotFoundException ignore) {
                    continue;
                }

                // 实例化被 @Client 标识的 client, 存入到 map 中
                Client client = clz.getAnnotation(Client.class);
                if (client != null) {
                    try {
                        Constructor constructor = clz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        OssClient uploader = (OssClient) constructor.newInstance();
                        OssClient.INSTANCES.put(client.value(), uploader);
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.trace("", e);
                    }
                }
            }
        }
    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 0.0.1
     */
    @Nullable
    public static OssClient getDeafultClient() {
        return getClient(MikPersistenComponent.getInstance().getState().getCloudType());
    }

    /**
     * Gets instance.
     *
     * @param cloudType the cloud type
     * @return the instance             有可能为 null
     * @since 0.0.1
     */
    @Nullable
    public static OssClient getClient(int cloudType) {
        return OssClient.INSTANCES.get(OssState.getCloudType(cloudType));
    }

    /**
     * 通过枚举获取 client 实例, 如果没有会从 subClassMap 中获取对应的 sub class name
     * 如果为 null, 则需要通过反射初始化, 避免通过 'test' 按钮失效
     *
     * @param cloudEnum the cloud enum
     * @return the instance             有可能返回 null
     * @since 0.0.1
     */
    @Nullable
    public static OssClient getClient(@NotNull CloudEnum cloudEnum) {
        OssClient client = OssClient.INSTANCES.get(cloudEnum);
        if(client == null){
            try {
                // 如果没有加载 class, 则执行 static 代码块
                Class<?> clz = Class.forName(cloudEnum.getFeature());

                Client clientAn = clz.getAnnotation(Client.class);
                // 被 @Client 标记过的 client 才执行 getInstance 静态方法
                if (clientAn != null) {
                    Method method = clz.getMethod("getInstance");
                    method.invoke(null);
                    client = OssClient.INSTANCES.get(cloudEnum);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    /**
     * Is enable boolean.
     *
     * @param client the client
     * @return the boolean
     * @since 0.0.1
     */
    @Contract("null -> false")
    public static boolean isEnable(OssClient client) {
        return client != null && OssState.getStatus(client.getCloudType());
    }

    /**
     * Is enable boolean.
     *
     * @param client the client
     * @return the boolean
     * @since 0.0.1
     */
    @Contract("null -> true")
    public static boolean isNotEnable(OssClient client) {
        return !isEnable(client);
    }
}
