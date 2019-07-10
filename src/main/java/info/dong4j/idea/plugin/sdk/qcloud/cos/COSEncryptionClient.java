package info.dong4j.idea.plugin.sdk.qcloud.cos;

import info.dong4j.idea.plugin.sdk.qcloud.cos.auth.COSCredentialsProvider;
import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.COSDirect;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto.COSCryptoModule;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto.CryptoConfiguration;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto.CryptoModuleDispatcher;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto.EncryptionMaterialsProvider;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto.QCLOUDKMS;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.AbortMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObject;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObjectId;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CompleteMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CompleteMultipartUploadResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyPartRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyPartResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.DeleteObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.EncryptedInitiateMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.GetObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.InitiateMultipartUploadRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.InitiateMultipartUploadResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.InstructionFileId;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutInstructionFileRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartResult;

import java.io.*;

public class COSEncryptionClient extends COSClient implements COSEncryption {

    private final COSCryptoModule crypto;
    /**
     * True if the a default KMS client is constructed, which will be shut down when this instance
     * of COS encryption client is shutdown. False otherwise, which means the users who provided the
     * KMS client would be responsible to shut down the KMS client.
     */
    private final boolean isKMSClientInternal;

    public COSEncryptionClient(COSCredentialsProvider credentialsProvider,
            EncryptionMaterialsProvider kekMaterialsProvider, ClientConfig clientConfig,
            CryptoConfiguration cryptoConfig) {
        this(null, credentialsProvider, kekMaterialsProvider, clientConfig, cryptoConfig);
    }

    public COSEncryptionClient(QCLOUDKMS kms, COSCredentialsProvider credentialsProvider,
            EncryptionMaterialsProvider kekMaterialsProvider, ClientConfig clientConfig,
            CryptoConfiguration cryptoConfig) {
        super(credentialsProvider.getCredentials(), clientConfig);
        assertParameterNotNull(kekMaterialsProvider,
                "EncryptionMaterialsProvider parameter must not be null.");
        assertParameterNotNull(cryptoConfig, "CryptoConfiguration parameter must not be null.");
        this.isKMSClientInternal = kms == null;
        this.crypto = new CryptoModuleDispatcher(kms, new COSDirectImpl(), credentialsProvider,
                kekMaterialsProvider, cryptoConfig);
    }

    private void assertParameterNotNull(Object parameterValue, String errorMessage) {
        if (parameterValue == null)
            throw new IllegalArgumentException(errorMessage);
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest req) {
        return crypto.putObjectSecurely(req.clone());
    }

    @Override
    public COSObject getObject(GetObjectRequest req) {
        return crypto.getObjectSecurely(req);
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest req, File dest) {
        return crypto.getObjectSecurely(req, dest);
    }

    @Override
    public void deleteObject(DeleteObjectRequest req) {
        // Delete the object
        super.deleteObject(req);
        // If it exists, delete the instruction file.
        InstructionFileId ifid =
                new COSObjectId(req.getBucketName(), req.getKey()).instructionFileId();

        DeleteObjectRequest instructionDeleteRequest = (DeleteObjectRequest) req.clone();
        instructionDeleteRequest.withBucketName(ifid.getBucket()).withKey(ifid.getKey());
        super.deleteObject(instructionDeleteRequest);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(
            CompleteMultipartUploadRequest req) {
        return crypto.completeMultipartUploadSecurely(req);
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(
            InitiateMultipartUploadRequest req) {
        boolean isCreateEncryptionMaterial = true;
        if (req instanceof EncryptedInitiateMultipartUploadRequest) {
            EncryptedInitiateMultipartUploadRequest cryptoReq =
                    (EncryptedInitiateMultipartUploadRequest) req;
            isCreateEncryptionMaterial = cryptoReq.isCreateEncryptionMaterial();
        }
        return isCreateEncryptionMaterial ? crypto.initiateMultipartUploadSecurely(req)
                : super.initiateMultipartUpload(req);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Because the encryption process requires context from block N-1 in order to encrypt block N,
     * parts uploaded with the COSEncryptionClient (as opposed to the normal COSClient) must be
     * uploaded serially, and in order. Otherwise, the previous encryption context isn't available
     * to use when encrypting the current part.
     */
    @Override
    public UploadPartResult uploadPart(UploadPartRequest uploadPartRequest)
            throws CosClientException {
        return crypto.uploadPartSecurely(uploadPartRequest);
    }

    @Override
    public CopyPartResult copyPart(CopyPartRequest copyPartRequest) {
        return crypto.copyPartSecurely(copyPartRequest);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest req) {
        crypto.abortMultipartUploadSecurely(req);
    }

    /**
     * Creates a new crypto instruction file by re-encrypting the CEK of an existing encrypted COS
     * object with a new encryption material identifiable via a new set of material description.
     * <p>
     * User of this method is responsible for explicitly deleting/updating the instruction file so
     * created should the corresponding COS object is deleted/created.
     *
     * @return the result of the put (instruction file) operation.
     */
    public PutObjectResult putInstructionFile(PutInstructionFileRequest req) {
        return crypto.putInstructionFileSecurely(req);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the a default internal KMS client has been constructed, it will also be shut down by
     * calling this method. Otherwise, users who provided the KMS client would be responsible to
     * shut down the KMS client extrinsic to this method.
     */
    @Override
    public void shutdown() {
        super.shutdown();
        // if (isKMSClientInternal)
        // kms.shutdown();
    }

    // /////////////////// Access to the methods in the super class //////////
    /**
     * An internal implementation used to provide limited but direct access to the underlying
     * methods of COSClient without any encryption or decryption operations.
     */
    private final class COSDirectImpl extends COSDirect {
        @Override
        public PutObjectResult putObject(PutObjectRequest req) {
            return COSEncryptionClient.super.putObject(req);
        }

        @Override
        public COSObject getObject(GetObjectRequest req) {
            return COSEncryptionClient.super.getObject(req);
        }

        @Override
        public ObjectMetadata getObject(GetObjectRequest req, File dest) {
            return COSEncryptionClient.super.getObject(req, dest);
        }

        @Override
        public CompleteMultipartUploadResult completeMultipartUpload(
                CompleteMultipartUploadRequest req) {
            return COSEncryptionClient.super.completeMultipartUpload(req);
        }

        @Override
        public InitiateMultipartUploadResult initiateMultipartUpload(
                InitiateMultipartUploadRequest req) {
            return COSEncryptionClient.super.initiateMultipartUpload(req);
        }

        @Override
        public UploadPartResult uploadPart(UploadPartRequest req)
                throws CosClientException {
            return COSEncryptionClient.super.uploadPart(req);
        }

        @Override
        public CopyPartResult copyPart(CopyPartRequest req) {
            return COSEncryptionClient.super.copyPart(req);
        }

        @Override
        public void abortMultipartUpload(AbortMultipartUploadRequest req) {
            COSEncryptionClient.super.abortMultipartUpload(req);
        }
    }

}
