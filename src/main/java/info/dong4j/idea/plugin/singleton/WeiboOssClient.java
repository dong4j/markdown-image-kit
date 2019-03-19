package info.dong4j.idea.plugin.singleton;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.WeiboOssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.UploadResponse;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;

import java.io.*;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-18 09:57
 */
@Slf4j
public class WeiboOssClient {
    private final Object lock = new Object();
    private static WbpUploadRequest ossClient = null;

    private WeiboOssClient() {
    }

    private static class SingletonHandler {
        static {
            init();
        }

        private static WeiboOssClient singleton = new WeiboOssClient();
    }

    /**
     * 如果是第一次使用, ossClient == null
     */
    private static void init() {
        WeiboOssState weiboOssState = ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState();
        String username = weiboOssState.getUserName();
        String password = DES.decrypt(weiboOssState.getPassword(), ImageManagerState.WEIBOKEY);

        try {
            ossClient = new UploadRequestBuilder()
                .setAcount(username, password)
                .build();
        } catch (Exception ignored) {
        }
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     */
    private boolean checkClient() {
        synchronized (lock) {
            if (ossClient == null) {
                init();
            }
            return ossClient != null;
        }
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    public void setOssClient(WbpUploadRequest oss) {
        ossClient = oss;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static WeiboOssClient getInstance() {
        return WeiboOssClient.SingletonHandler.singleton;
    }

    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @throws IOException the io exception
     */
    public String upload(InputStream inputStream, String fileName) {
        return upload(ossClient, inputStream, fileName);
    }

    /**
     * Upload string.
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @throws IOException the io exception
     */
    public String upload(WbpUploadRequest ossClient, InputStream inputStream, String fileName) {
        File file = new File(System.getProperty("java.io.tmpdir") + fileName);
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
            return upload(ossClient, file);
        } catch (IOException e) {
            log.trace("", e);
        }
        return "";
    }

    /**
     * Upload string.
     *
     * @param file the file
     * @return the string
     * @throws IOException the io exception
     */
    public String upload(File file) {
        return upload(ossClient, file);
    }

    /**
     * Upload string.
     *
     * @param ossClient the oss client
     * @param file      the file
     * @return the string
     * @throws IOException the io exception
     */
    public String upload(WbpUploadRequest ossClient, File file)  {
        String url = "";
        UploadResponse response;
        try {
            response = ossClient.upload(file);
            if (response.getResult().equals(UploadResponse.ResultStatus.SUCCESS)) {
                url = response.getImageInfo().getLarge();
            }
        } catch (IOException e) {
            log.trace("", e);
        }
        return url;
    }
}
