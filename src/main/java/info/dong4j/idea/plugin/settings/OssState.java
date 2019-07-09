/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

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
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019 -03-19 18:59
 */
@Data
public abstract class OssState {
    private boolean passedTest = false;
    private Map<String, String> oldAndNewAuthInfo = new HashMap<>(2);

    /**
     * 通过2个状态判断客户端是否可用
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
     * Get state.
     *
     * @param cloudIndex the cloud index
     */
    public static void getState(int cloudIndex){
        CloudEnum cloudEnum = getCloudType(cloudIndex);

    }

    /**
     * 获取当前图床的可用状态
     *
     * @param cloudIndex the cloud index
     * @return the boolean
     */
    @Contract(pure = true)
    public static boolean getStatus(int cloudIndex) {
        if(cloudIndex == CloudEnum.SM_MS_CLOUD.index){
            return true;
        }
        return getStatus(getCloudType(cloudIndex));
    }

    /**
     * 获取当前图床的可用状态
     *
     * @param cloudIndex the cloud index
     * @return the boolean
     */
    @Contract(pure = true)
    public static boolean getStatusBySetting(int cloudIndex) {
        if(cloudIndex == CloudEnum.SM_MS_CLOUD.index){
            return true;
        }
        return getStatusBySetting(getCloudType(cloudIndex));
    }

    /**
     * 通过 index 获取枚举, 不会返回 null, 业务上控制了, 只会选择 CloudEnum 有的类型
     *
     * @param cloudIndex the cloud index
     * @return the cloud enum
     */
    public static CloudEnum getCloudType(int cloudIndex) {
        Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == cloudIndex);
        return cloudType.orElse(null);
    }

    /**
     * Get default cloud cloud enum.
     *
     * @return the cloud enum
     */
    public static CloudEnum getDefaultCloud() {
        Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == MikPersistenComponent.getInstance().getState().getCloudType());
        return cloudType.orElse(null);
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
               && StringUtils.isNotEmpty(oldAndNewAuth.get(MikState.OLD_HASH_KEY))
               && oldAndNewAuth.get(MikState.OLD_HASH_KEY).equals(oldAndNewAuth.get(MikState.NEW_HASH_KEY));
    }

    /**
     * 在设置页面获取当前图床的可用状态
     *
     * @param cloudEnum the cloud enum
     * @return the boolean
     */
    @Contract(pure = true)
    public static boolean getStatusBySetting(CloudEnum cloudEnum) {
        MikState state = MikPersistenComponent.getInstance().getState();
        if (cloudEnum == null) {
            return false;
        }

        switch (cloudEnum) {
            case WEIBO_CLOUD:
                return state.getWeiboOssState().isPassedTest();
            case ALIYUN_CLOUD:
                return state.getAliyunOssState().isPassedTest();
            case QINIU_CLOUD:
                return state.getQiniuOssState().isPassedTest();
            case WANGYI_CLOUD:
                return false;
            case BAIDU_CLOUD:
                return false;
            case JINGDONG_CLOUD:
                return false;
            case YOUPAI_CLOUD:
                return false;
            case SM_MS_CLOUD:
                return true;
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

    /**
     * Gets status.
     *
     * @param cloudEnum the cloud enum
     * @return the status
     */
    @Contract(pure = true)
    public static boolean getStatus(CloudEnum cloudEnum) {
        MikState state = MikPersistenComponent.getInstance().getState();
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
            case TENCENT_CLOUD:
                return getStatus(state.getTencentOssState());
            case WANGYI_CLOUD:
                return false;
            case BAIDU_CLOUD:
                return false;
            case JINGDONG_CLOUD:
                return false;
            case YOUPAI_CLOUD:
                return false;
            case SM_MS_CLOUD:
                return true;
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
