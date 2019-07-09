package info.dong4j.idea.plugin.sdk.qcloud.cos.demo;

import info.dong4j.idea.plugin.sdk.qcloud.cos.COSClient;
import info.dong4j.idea.plugin.sdk.qcloud.cos.ClientConfig;
import info.dong4j.idea.plugin.sdk.qcloud.cos.auth.BasicCOSCredentials;
import info.dong4j.idea.plugin.sdk.qcloud.cos.auth.COSCredentials;
import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosServiceException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObjectSummary;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ListObjectsRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectListing;
import info.dong4j.idea.plugin.sdk.qcloud.cos.region.Region;

import java.util.List;

/**
 * ListObjectsDemo展示了如何列出object
 */
public class ListObjectsDemo {

    public static void listObjectsDemo() {
        // 1 初始化用户身份信息(appid, secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials("AKIDXXXXXXXX", "1A2Z3YYYYYYYYYY");
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing-1"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket名称, 需包含appid
        String bucketName = "mybucket-1251668577";

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置bucket名称
        listObjectsRequest.setBucketName(bucketName);
        // prefix表示列出的object的key以prefix开始
        listObjectsRequest.setPrefix("");
        // 设置最大遍历出多少个对象, 一次listobject最大支持1000
        listObjectsRequest.setMaxKeys(1000);
        // listObjectsRequest.setDelimiter("/");
        ObjectListing objectListing = null;
        try {
            objectListing = cosclient.listObjects(listObjectsRequest);
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
        // common prefix表示表示被delimiter截断的路径, 如delimter设置为/, common prefix则表示所有子目录的路径
        List<String> commonPrefixs = objectListing.getCommonPrefixes();

        // object summary表示所有列出的object列表
        List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
        for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
            // 文件的路径key
            String key = cosObjectSummary.getKey();
            // 文件的etag
            String etag = cosObjectSummary.getETag();
            // 文件的长度
            long fileSize = cosObjectSummary.getSize();
            // 文件的存储类型
            String storageClasses = cosObjectSummary.getStorageClass();

            System.out.println("key: " + key);
        }

        cosclient.shutdown();
    }

    // 如果要获取超过maxkey数量的object或者获取所有的object, 则需要循环调用listobject, 用上一次返回的next marker作为下一次调用的marker,
    // 直到返回的truncated为false
    public static void listAllObjects() {
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials("AKIDXXXXXXXX", "1A2Z3YYYYYYYYYY");
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing-1"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket名需包含appid
        String bucketName = "mybucket-1251668577";

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置bucket名称
        listObjectsRequest.setBucketName(bucketName);
        // prefix表示列出的object的key以prefix开始
        listObjectsRequest.setPrefix("");
        // deliter表示分隔符, 设置为/表示列出当前目录下的object, 设置为空表示列出所有的object
        listObjectsRequest.setDelimiter("");
        // 设置最大遍历出多少个对象, 一次listobject最大支持1000
        listObjectsRequest.setMaxKeys(1000);
        ObjectListing objectListing = null;
        do {

            try {
                objectListing = cosclient.listObjects(listObjectsRequest);
            } catch (CosServiceException e) {
                e.printStackTrace();
                return;
            } catch (CosClientException e) {
                e.printStackTrace();
                return;
            }
            // common prefix表示表示被delimiter截断的路径, 如delimter设置为/, common prefix则表示所有子目录的路径
            List<String> commonPrefixs = objectListing.getCommonPrefixes();

            // object summary表示所有列出的object列表
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                // 文件的路径key
                String key = cosObjectSummary.getKey();
                // 文件的etag
                String etag = cosObjectSummary.getETag();
                // 文件的长度
                long fileSize = cosObjectSummary.getSize();
                // 文件的存储类型
                String storageClasses = cosObjectSummary.getStorageClass();
            }

            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());

        cosclient.shutdown();
    }

    public static void main(String[] args) {
        listAllObjects();
    }

}
