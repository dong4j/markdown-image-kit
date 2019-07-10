package info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto;

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
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutInstructionFileRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartResult;

import java.io.*;

public abstract class COSCryptoModule {
    /**
     * @return the result of the putting the COS object.
     */
    public abstract PutObjectResult putObjectSecurely(PutObjectRequest req);

    public abstract COSObject getObjectSecurely(GetObjectRequest req);

    public abstract ObjectMetadata getObjectSecurely(GetObjectRequest req,
            File dest);

    public abstract CompleteMultipartUploadResult completeMultipartUploadSecurely(
            CompleteMultipartUploadRequest req);

    public abstract InitiateMultipartUploadResult initiateMultipartUploadSecurely(
            InitiateMultipartUploadRequest req);

    public abstract UploadPartResult uploadPartSecurely(UploadPartRequest req);

    public abstract CopyPartResult copyPartSecurely(CopyPartRequest req);

    public abstract void abortMultipartUploadSecurely(AbortMultipartUploadRequest req);

    /**
     * @return the result of putting the instruction file in COS; or null if the
     *         specified COS object doesn't exist. The COS object can be
     *         subsequently retrieved using the new instruction file via the
     *         usual get operation by specifying a
     *         {@link EncryptedGetObjectRequest}.
     *
     * @throws IllegalArgumentException
     *             if the specified COS object doesn't exist.
     * @throws SecurityException
     *             if the protection level of the material in the new
     *             instruction file is lower than that of the original.
     *             Currently, this means if the original material has been
     *             secured via authenticated encryption, then the new
     *             instruction file cannot be created via an COS encryption
     *             client configured with {@link CryptoMode#EncryptionOnly}.
     */
    public abstract PutObjectResult putInstructionFileSecurely(
            PutInstructionFileRequest req);

}
