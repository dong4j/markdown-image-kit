package info.dong4j.idea.plugin.strategy;

import info.dong4j.idea.plugin.client.OssClient;

import org.jetbrains.annotations.Contract;

import java.io.*;

import javax.swing.JPanel;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 从测试按钮发起的上传请求</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 13:14
 */
public class UploadFromTest implements UploadWay {
    private OssClient client;
    private InputStream inputStream;
    private String fileName;
    private JPanel jPanel;

    /**
     * Instantiates a new Upload from test.
     *
     * @param client      the client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     */
    @Contract("null, _, _, _ -> fail")
    public UploadFromTest(OssClient client, InputStream inputStream, String fileName, JPanel jPanel) {
        assert client != null;
        this.client = client;
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.jPanel = jPanel;
    }

    @Override
    public String upload() {
        return client.upload(inputStream, fileName, jPanel);
    }
}
