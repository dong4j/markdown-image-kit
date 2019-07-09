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

public abstract class COSDirect implements COSDirectSpi {
    public abstract PutObjectResult putObject(PutObjectRequest req);

    public abstract COSObject getObject(GetObjectRequest req);

    public abstract ObjectMetadata getObject(GetObjectRequest req, File dest);

    public abstract CompleteMultipartUploadResult completeMultipartUpload(
            CompleteMultipartUploadRequest req);

    public abstract InitiateMultipartUploadResult initiateMultipartUpload(
            InitiateMultipartUploadRequest req);

    public abstract UploadPartResult uploadPart(UploadPartRequest req);

    public abstract CopyPartResult copyPart(CopyPartRequest req);

    public abstract void abortMultipartUpload(AbortMultipartUploadRequest req);
}
