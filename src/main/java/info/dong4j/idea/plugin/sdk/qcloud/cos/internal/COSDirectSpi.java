package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.model.AbortMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObject;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CompleteMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CompleteMultipartUploadResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyPartRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyPartResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.GetObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.InitiateMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.InitiateMultipartUploadResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartResult;

import java.io.*;

/**
 * A Service Provider Interface that allows direct access to the underlying non-encrypting COS
 * client of an COS encryption client instance.
 */
public interface COSDirectSpi {
    PutObjectResult putObject(PutObjectRequest req);

    COSObject getObject(GetObjectRequest req);

    ObjectMetadata getObject(GetObjectRequest req, File dest);

    CompleteMultipartUploadResult completeMultipartUpload(
        CompleteMultipartUploadRequest req);

    InitiateMultipartUploadResult initiateMultipartUpload(
        InitiateMultipartUploadRequest req);

    UploadPartResult uploadPart(UploadPartRequest req);

    CopyPartResult copyPart(CopyPartRequest req);

    void abortMultipartUpload(AbortMultipartUploadRequest req);
}
