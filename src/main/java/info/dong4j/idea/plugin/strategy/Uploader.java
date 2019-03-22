package info.dong4j.idea.plugin.strategy;

import org.jetbrains.annotations.Contract;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 13:16
 */
@Slf4j
public class Uploader {
    private UploadWay uploadWay;

    private Uploader() {}

    private static class SingletonHandler {
        private static Uploader singleton = new Uploader();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static Uploader getInstance() {
        return Uploader.SingletonHandler.singleton;
    }

    /**
     * Set upload way uploader.
     *
     * @param uploadWay the upload way
     * @return the uploader
     */
    public Uploader setUploadWay(UploadWay uploadWay) {
        this.uploadWay = uploadWay;
        return this;
    }

    /**
     * Upload string.
     *
     * @return the string
     */
    public String upload() {
        return uploadWay.upload();
    }
}
