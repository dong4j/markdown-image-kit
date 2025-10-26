package info.dong4j.idea.plugin.settings;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Data;

/**
 * OssState 类
 * <p>
 * 用于表示对象存储服务（OSS）的状态信息，提供客户端可用性判断相关功能。该类封装了状态测试结果和授权信息，支持通过索引或枚举类型获取当前图床的可用状态。
 * <p>
 * 主要功能包括：
 * 1. 判断客户端是否通过状态测试
 * 2. 存储和获取旧版与新版授权信息
 * 3. 根据云服务类型索引或枚举获取可用状态
 * 4. 提供默认云服务类型的获取方法
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Data
public abstract class OssState {
    /** 是否通过测试 */
    private boolean passedTest = false;
    /** 保存旧的和新的认证信息，用于对比或回滚操作 */
    private Map<String, String> oldAndNewAuthInfo = new HashMap<>(2);

    /**
     * 根据两个状态判断客户端是否可用，并保存状态信息
     * <p>
     * 该方法用于设置状态的测试通过标志，并将键值对存入状态对象的认证信息中
     *
     * @param state    状态对象，用于存储测试结果和认证信息
     * @param hashcode 哈希码，作为键值对中的值
     * @param key      键，用于关联哈希码
     */
    public static void saveStatus(OssState state, int hashcode, String key) {
        state.setPassedTest(true);
        state.getOldAndNewAuthInfo().put(key, String.valueOf(hashcode));
    }

    /**
     * 获取当前图床的可用状态
     * <p>
     * 根据传入的云索引判断对应图床是否可用。若索引匹配SM_MS_CLOUD，则返回true；否则递归获取对应云类型的可用状态。
     *
     * @param cloudIndex 云索引
     * @return 图床是否可用
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static boolean getStatus(int cloudIndex) {
        if(cloudIndex == CloudEnum.SM_MS_CLOUD.index){
            return true;
        }
        return getStatus(getCloudType(cloudIndex));
    }

    /**
     * 根据给定的云索引获取对应的枚举值
     * <p>
     * 该方法通过传入的索引查找对应的 CloudEnum 枚举对象，由于业务逻辑已确保索引有效，因此不会返回 null。
     *
     * @param cloudIndex 云索引值
     * @return 对应的 CloudEnum 枚举对象
     * @since 0.0.1
     */
    public static CloudEnum getCloudType(int cloudIndex) {
        Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == cloudIndex);
        return cloudType.orElse(null);
    }

    /**
     * 获取默认的云类型枚举
     * <p>
     * 通过获取当前状态中的云类型索引，查找对应的云类型枚举对象，若未找到则返回 null。
     *
     * @return 云类型枚举对象，若未找到则返回 null
     * @since 0.0.1
     */
    public static CloudEnum getDefaultCloud() {
        Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == MikPersistenComponent.getInstance().getState().getCloudType());
        return cloudType.orElse(null);
    }

    /**
     * 根据状态对象判断当前是否为可用状态
     * <p>
     * 通过检查状态对象中的测试是否通过、旧哈希值和新哈希值是否相等来判断是否为可用状态
     *
     * @param state 状态对象
     * @return 如果状态可用返回 true，否则返回 false
     */
    public static boolean getStatus(OssState state) {
        boolean isPassedTest = state.isPassedTest();
        Map<String, String> oldAndNewAuth = state.getOldAndNewAuthInfo();
        return isPassedTest
               && StringUtils.isNotEmpty(oldAndNewAuth.get(MikState.OLD_HASH_KEY))
               && oldAndNewAuth.get(MikState.OLD_HASH_KEY).equals(oldAndNewAuth.get(MikState.NEW_HASH_KEY));
    }

    /**
     * 根据云服务商枚举获取对应状态
     * <p>
     * 根据传入的云服务商枚举值，获取对应云服务的状态信息
     *
     * @param cloudEnum 云服务商枚举
     * @return 对应云服务的状态值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static boolean getStatus(CloudEnum cloudEnum) {
        MikState state = MikPersistenComponent.getInstance().getState();
        if (cloudEnum == null) {
            return false;
        }

        // todo-dong4j : (2025.10.26 17:53) [将 false 的服务商删除]
        return switch (cloudEnum) {
            case SM_MS_CLOUD -> true;
            case ALIYUN_CLOUD -> getStatus(state.getAliyunOssState());
            case QINIU_CLOUD -> getStatus(state.getQiniuOssState());
            case TENCENT_CLOUD -> getStatus(state.getTencentOssState());
            case BAIDU_CLOUD -> getStatus(state.getBaiduBosState());
            case GITHUB -> getStatus(state.getGithubOssState());
            case GITEE -> getStatus(state.getGiteeOssState());
            case CUSTOMIZE -> getStatus(state.getCustomOssState());
        };
    }
}
