package info.dong4j.idea.plugin.settings;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.EnumsUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Data;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-19 18:59
 */
@Data
public abstract class OssState {
    private boolean passedTest = false;
    private Map<String, String> oldAndNewAuthInfo = new HashMap<>(2);

    /**
     * 保存按钮是否可用
     *
     * @param state    the state
     * @param hashcode the hashcode
     * @param key      the key
     */
    public static void saveStatus(OssState state, int hashcode, String key) {
        state.setPassedTest(true);
        state.getOldAndNewAuthInfo().put(key, String.valueOf(hashcode));
    }

    /**
     * 获取当前图床的可用状态
     *
     * @param cloudIndex the cloud index
     * @return the boolean
     */
    @Contract(pure = true)
    public static boolean getStatus(int cloudIndex) {
        Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == cloudIndex);
        CloudEnum cloudEnum = cloudType.orElse(null);
        return getStatus(cloudEnum);
    }

    /**
     * 根据 state 获取当前可用状态
     *
     * @param state the state
     * @return the boolean
     */
    public static boolean getStatus(OssState state) {
        boolean isPassedTest = state.isPassedTest();
        Map<String, String> oldAndNewAuth = state.getOldAndNewAuthInfo();
        return isPassedTest
               && StringUtils.isNotEmpty(oldAndNewAuth.get(ImageManagerState.OLD_HASH_KEY))
               && oldAndNewAuth.get(ImageManagerState.OLD_HASH_KEY).equals(oldAndNewAuth.get(ImageManagerState.NEW_HASH_KEY));
    }

    /**
     * 获取当前图床的可用状态
     * todo-dong4j : (2019年03月20日 15:47) [增加图床实现时记得改这里]
     *
     * @param cloudEnum the cloud enum
     * @return the boolean
     */
    @Contract(pure = true)
    public static boolean getStatus(CloudEnum cloudEnum) {
        ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
        if (cloudEnum == null) {
            return false;
        }
        switch (cloudEnum) {
            case WEIBO_CLOUD:
                return getStatus(state.getWeiboOssState());
            case ALIYUN_CLOUD:
                return getStatus(state.getAliyunOssState());
            case QINIU_CLOUD:
                return getStatus(state.getQiniuOssState());
            case WANGYI_CLOUD:
                return getStatus(state.getWeiboOssState());
            case BAIDU_CLOUD:
                return false;
            case JINGDONG_CLOUD:
                return false;
            case YOUPAI_CLOUD:
                return false;
            case SM_MS_CLOUD:
                return false;
            case IMGUR_CLOUD:
                return false;
            case U_CLOUD:
                return false;
            case QING_CLOUD:
                return false;
            case CUSTOMIZE:
                return false;
            default:
                return false;
        }
    }
}
