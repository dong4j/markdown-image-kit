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
 * 客户端工具类
 * <p>
 * 提供与 OssClient 相关的客户端实例管理功能，包括通过反射创建并缓存客户端实例、根据云类型或枚举获取对应的客户端实例、判断客户端是否可用等。
 * 该类通过静态方法实现，支持通过反射机制动态加载客户端类，并根据注解信息进行实例化和缓存。
 * <p>
 * 主要功能包括：
 * 1. 通过反射生成客户端实例并缓存
 * 2. 根据云类型获取对应的客户端实例
 * 3. 根据枚举获取对应的客户端实例
 * 4. 判断客户端是否可用
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@SuppressWarnings( {"unused", "D"})
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
     * 使用反射生成 client，缓存 client 关系
     * <p>
     * 该方法通过传入的类名列表，利用反射机制加载对应的类，并检查是否为可实例化的 OssClient 子类。
     * 如果类满足条件，则根据其上的 @Client 注解创建实例，并将实例存入全局缓存 map 中。
     *
     * @param clsList 类名列表，用于反射加载对应的类
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
     * 获取默认的OssClient实例
     * <p>
     * 通过获取当前状态中的云类型，返回对应的OssClient实例
     *
     * @return OssClient实例，可能为null
     * @since 0.0.1
     */
    @Nullable
    public static OssClient getDeafultClient() {
        return getClient(MikPersistenComponent.getInstance().getState().getDefaultCloudType());
    }

    /**
     * 根据云类型获取对应的OssClient实例
     * <p>
     * 通过传入的云类型参数，查找并返回对应的OssClient实例。如果未找到对应实例，可能返回null。
     *
     * @param cloudType 云类型参数
     * @return 对应的OssClient实例，可能为null
     * @since 0.0.1
     */
    @Nullable
    public static OssClient getClient(int cloudType) {
        return OssClient.INSTANCES.get(OssState.getCloudType(cloudType));
    }

    /**
     * 通过枚举获取 OssClient 实例，若未找到则从 subClassMap 中获取对应的子类名称
     * 若结果为 null，则通过反射初始化，避免因 'test' 按钮失效
     *
     * @param cloudEnum 云枚举对象，用于确定要获取的客户端类型
     * @return OssClient 实例，可能为 null
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
     * 判断OSS客户端是否启用
     * <p>
     * 检查传入的OSS客户端对象是否为非空，并根据其云类型获取对应的状态，判断是否启用
     *
     * @param client OSS客户端对象
     * @return 如果客户端非空且状态为启用，返回true；否则返回false
     * @since 0.0.1
     */
    @Contract("null -> false")
    public static boolean isEnable(OssClient client) {
        return client != null && OssState.getStatus(client.getCloudType());
    }

    /**
     * 判断OSS客户端是否未启用
     * <p>
     * 调用该方法可检查给定的OSS客户端对象是否未启用。若客户端为null，则默认返回true。
     *
     * @param client OSS客户端对象
     * @return 如果客户端未启用返回true，否则返回false
     * @since 0.0.1
     */
    @Contract("null -> true")
    public static boolean isNotEnable(OssClient client) {
        return !isEnable(client);
    }
}
