package info.dong4j.idea.plugin.strategy;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 目前有 3 种上传途径, 以后可能还会增加, 这里使用策略模式扩展</p>
 *
 * @author dong4j
 * @date 2019 -03-22 13:12
 * @email sjdong3 @iflytek.com
 */
public interface UploadWay {
    /**
     * Upload.
     *
     * @return the string
     */
    String upload();
}
