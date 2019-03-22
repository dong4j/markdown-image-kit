package info.dong4j.idea.plugin.singleton;

import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 09:22
 */
@Slf4j
public abstract class AbstractOssClient implements OssClient {

    /**
     * 通过枚举获取 client 实例, 必须在 CloudEnum 写 client 的实现类全类名
     * todo-dong4j : (2019年03月22日 09:36) [考虑迁移到配置文件]
     *
     * @param cloudEnum the cloud enum
     * @return the instance
     */
    @Nullable
    public static OssClient getInstance(@NotNull CloudEnum cloudEnum) {
        String className = cloudEnum.getClassName();
        try {
            Class<?> cls = Class.forName(className);
            OssClient uploader = OssClient.INSTANCES.get(className);
            if (uploader == null) {
                Constructor constructor = cls.getDeclaredConstructor();
                constructor.setAccessible(true);
                uploader = (OssClient) constructor.newInstance();
                OssClient.INSTANCES.put(className, uploader);
            }
            return uploader;
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            log.trace("", e);
        }
        return null;
    }
}
