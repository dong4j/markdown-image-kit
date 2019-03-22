package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.client.Client;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.client.WeiboOssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 16:22
 */
@Slf4j
public final class ClientUtils {
    private static AtomicBoolean isInit = new AtomicBoolean(false);

    static {
        if (!isInit.get()) {
            try {
                // 获取特定包下所有的类(包括接口和类)
                cache(ClassUtils.getAllClassByInterface(OssClient.class));
            } catch (Exception e) {
                log.trace("", e);
            }
            isInit.set(true);
        }
    }

    /**
     * 通过直接将缓存 client 关系
     *
     * @param clsList    the cls list
     */
    private static void cache(List<String> clsList) {
        if (clsList != null && clsList.size() > 0) {
            for (String className : clsList) {
                Class<?> clz;
                try {
                    clz = Class.forName(className);
                    // 抽象类忽略
                    if (Modifier.isAbstract(clz.getModifiers())) {
                        continue;
                    }
                    // 接口忽略
                    if (Modifier.isInterface(clz.getModifiers())) {
                        continue;
                    }
                    if (!OssClient.class.isAssignableFrom(clz)) {
                        continue;
                    }
                } catch (ClassNotFoundException ignore) {
                    continue;
                }

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
     * 通过枚举获取 client 实例, 如果没有会从 subClassMap 中获取对应的 sub class name
     * todo-dong4j : (2019年03月22日 09:36) [考虑迁移到配置文件]
     *
     * @param cloudEnum the cloud enum
     * @return the instance
     */
    @Nullable
    public static OssClient getInstance(@NotNull CloudEnum cloudEnum) {
        OssClient uploader = OssClient.INSTANCES.get(cloudEnum);
        return uploader == null ? WeiboOssClient.getInstance() : uploader;
    }
}
