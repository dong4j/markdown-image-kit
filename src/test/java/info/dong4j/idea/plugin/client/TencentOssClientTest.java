/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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

package info.dong4j.idea.plugin.client;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.AnonymousCOSCredentials;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;

import info.dong4j.idea.plugin.enums.CloudEnum;

import org.junit.Test;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 腾讯与 COS 简单上传文件 </p>
 *
 * @author dong4j
 * @date 2019-04-16 20:11
 * @email sjdong3@iflytek.com
 */
@Slf4j
public class TencentOssClientTest {
    private static String secretId = System.getProperty("secretId");
    private static String secretKey = System.getProperty("secretKey");
    // bucket名需包含appid
    private static String bucketName = System.getProperty("bucketName");

    private static void SimpleUploadFileFromLocal() {
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-chengdu"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        String key = "x1.png";
        File localFile = new File("/Users/dong4j/Downloads/xu.png");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        // 设置存储类型, 默认是标准(Standard), 低频(standard_ia)
        putObjectRequest.setStorageClass(StorageClass.Standard);
        try {
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
            // putobjectResult会返回文件的etag
            String etag = putObjectResult.getETag();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 关闭客户端
        cosclient.shutdown();
    }

    // 从输入流进行读取并上传到COS
    private static void SimpleUploadFileFromStream() {
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials("AKIDXXXXXXXX", "1A2Z3YYYYYYYYYY");
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing-1"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket名需包含appid
        String bucketName = "mybucket-1251668577";

        String key = "aaa/bbb.jpg";
        File localFile = new File("src/test/resources/len10M.txt");

        InputStream input = new ByteArrayInputStream(new byte[10]);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        // 从输入流上传必须制定content length, 否则http客户端可能会缓存所有数据，存在内存OOM的情况
        objectMetadata.setContentLength(10);
        // 默认下载时根据cos路径key的后缀返回响应的contenttype, 上传时设置contenttype会覆盖默认值
        objectMetadata.setContentType("image/jpeg");

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, input, objectMetadata);
        // 设置存储类型, 默认是标准(Standard), 低频(standard_ia)
        putObjectRequest.setStorageClass(StorageClass.Standard_IA);
        try {
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
            // putobjectResult会返回文件的etag
            String etag = putObjectResult.getETag();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 关闭客户端
        cosclient.shutdown();
    }

    private static String buildUrl(String key) {
        // 生成匿名的请求签名，需要重新初始化一个匿名的 cosClient
        // 1 初始化用户身份信息, 匿名身份不用传入ak sk
        COSCredentials cred = new AnonymousCOSCredentials();
        // 2 设置 bucket 的区域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-chengdu"));
        // 3 生成 cos 客户端
        COSClient cosClient = new COSClient(cred, clientConfig);

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest("dong4j-1258270892", key, HttpMethodName.GET);
        URL url = cosClient.generatePresignedUrl(req);

        cosClient.shutdown();
        return url.toString();
    }

    @Test
    public void test() {
        SimpleUploadFileFromLocal();
        // url = <BucketName-APPID>.cos.region_name.myqcloud.com/key
        log.info("url = {}", buildUrl("xu.png"));
    }

    @Test
    public void test2() {
        // 实例化被 @Client 标识的 client, 存入到 map 中
        Class<?> clz = null;
        try {
            clz = Class.forName(CloudEnum.TENCENT_CLOUD.feature);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Constructor constructor = clz.getDeclaredConstructor();
            constructor.setAccessible(true);
            OssClient uploader = (OssClient) constructor.newInstance();

            OssClient.INSTANCES.put(CloudEnum.TENCENT_CLOUD, uploader);

            upload();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.trace("", e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void upload() throws FileNotFoundException {
        OssClient uploader = OssClient.INSTANCES.get(CloudEnum.TENCENT_CLOUD);
        log.info("{}", uploader.getName());
        String url = uploader.upload(new FileInputStream(new File("/Users/dong4j/Downloads/xu.png")), "x2.png");
        log.info("url = {}", url);
    }
}