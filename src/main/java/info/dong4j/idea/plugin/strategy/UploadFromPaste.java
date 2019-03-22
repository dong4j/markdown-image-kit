package info.dong4j.idea.plugin.strategy;

import info.dong4j.idea.plugin.client.OssClient;

import org.jetbrains.annotations.Contract;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 从 paste 发起的上传请求 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 13:14
 */
public class UploadFromPaste implements UploadWay{
    private OssClient client;
    private InputStream inputStream;
    private String fileName;

    /**
     * Instantiates a new Upload from paste.
     *
     * @param client      the client
     * @param inputStream the input stream
     * @param fileName    the file name
     */
    @Contract("null, _, _ -> fail")
    public UploadFromPaste(OssClient client, InputStream inputStream, String fileName){
        assert client != null;
        this.client = client;
        this.inputStream = inputStream;
        this.fileName = fileName;
    }

    @Override
    public String upload() {
        return client.upload(inputStream, fileName);
    }
}
