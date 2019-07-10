package info.dong4j.idea.plugin.sdk.qcloud.cos;

/**
 * Common COS HTTP header values used throughout the COS Java client.
 */
public interface Headers {

    /*
     * Standard HTTP Headers
     */

    String HOST = "Host";
    String CACHE_CONTROL = "Cache-Control";
    String CONTENT_DISPOSITION = "Content-Disposition";
    String CONTENT_ENCODING = "Content-Encoding";
    String CONTENT_LENGTH = "Content-Length";
    String CONTENT_RANGE = "Content-Range";
    String CONTENT_MD5 = "Content-MD5";
    String CONTENT_TYPE = "Content-Type";
    String CONTENT_LANGUAGE = "Content-Language";
    String DATE = "Date";
    String ETAG = "ETag";
    String LAST_MODIFIED = "Last-Modified";
    String SERVER = "Server";
    String USER_AGENT = "User-Agent";
    String SDK_LOG_DEBUG = "x-cos-sdk-log-debug";

    /*
     * Cos HTTP Headers
     */

    /** Prefix for general COS headers: x-cos- */
    String COS_PREFIX = "x-cos-";

    /** COS's canned ACL header: x-cos-acl */
    String COS_CANNED_ACL = "x-cos-acl";

    /** Cos's alternative date header: x-cos-date */
    String COS_ALTERNATE_DATE = "x-cos-date";

    /** Prefix for COS user metadata: x-cos-meta- */
    String COS_USER_METADATA_PREFIX = "x-cos-meta-";

    /** COS's version ID header */
    String COS_VERSION_ID = "x-cos-version-id";

    /** COS's Multi-Factor Authentication header */
    String COS_AUTHORIZATION = "Authorization";

    /** COS response header for a request's cos request ID */
    String REQUEST_ID = "x-cos-request-id";

    /** COS response header for TRACE ID */
    String TRACE_ID = "x-cos-trace-id";

    /** COS request header indicating how to handle metadata when copying an object */
    String METADATA_DIRECTIVE = "x-cos-metadata-directive";

    /** DevPay token header */
    String SECURITY_TOKEN = "x-cos-security-token";

    /** Header describing what class of storage a user wants */
    String STORAGE_CLASS = "x-cos-storage-class";

    /** Header for optional server-side encryption algorithm */
    String SERVER_SIDE_ENCRYPTION = "x-cos-server-side-encryption";

    /** Header for the encryption algorithm used when encrypting the object with customer-provided keys */
    String SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM = "x-cos-server-side-encryption-customer-algorithm";

    /** Header for the customer-provided key for server-side encryption */
    String SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY = "x-cos-server-side-encryption-customer-key";

    /** Header for the MD5 digest of the customer-provided key for server-side encryption */
    String SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5 = "x-cos-server-side-encryption-customer-key-MD5";

    /** Header for the encryption algorithm used when encrypting the object with customer-provided keys */
    String COPY_SOURCE_SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM = "x-cos-copy-source-server-side-encryption-customer-algorithm";

    /** Header for the customer-provided key for server-side encryption */
    String COPY_SOURCE_SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY = "x-cos-copy-source-server-side-encryption-customer-key";

    /** Header for the MD5 digest of the customer-provided key for server-side encryption */
    String COPY_SOURCE_SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5 = "x-cos-copy-source-server-side-encryption-customer-key-MD5";

    /** Header for optional object expiration */
    String EXPIRATION = "x-cos-expiration";

    /** Header for optional object expiration */
    String EXPIRES = "Expires";

    /** ETag matching constraint header for the copy object request */
    String COPY_SOURCE_IF_MATCH = "x-cos-copy-source-if-match";

    /** ETag non-matching constraint header for the copy object request */
    String COPY_SOURCE_IF_NO_MATCH = "x-cos-copy-source-if-none-match";

    /** Unmodified since constraint header for the copy object request */
    String COPY_SOURCE_IF_UNMODIFIED_SINCE = "x-cos-copy-source-if-unmodified-since";

    /** Modified since constraint header for the copy object request */
    String COPY_SOURCE_IF_MODIFIED_SINCE = "x-cos-copy-source-if-modified-since";

    /** Range header for the get object request */
    String RANGE = "Range";

    /**Range header for the copy part request */
    String COPY_PART_RANGE = "x-cos-copy-source-range";

    /** Modified since constraint header for the get object request */
    String GET_OBJECT_IF_MODIFIED_SINCE = "If-Modified-Since";

    /** Unmodified since constraint header for the get object request */
    String GET_OBJECT_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    /** ETag matching constraint header for the get object request */
    String GET_OBJECT_IF_MATCH = "If-Match";

    /** ETag non-matching constraint header for the get object request */
    String GET_OBJECT_IF_NONE_MATCH = "If-None-Match";

    /** Encrypted symmetric key header that is used in the envelope encryption mechanism */
    String CRYPTO_KEY = "x-cos-key";

    /**
     * Encrypted symmetric key header that is used in the Authenticated
     * Encryption (AE) cryptographic module. Older versions of COS encryption
     * client with encryption-only capability would not be able to recognize
     * this AE key, and therefore will be prevented from mistakenly decrypting
     * ciphertext in AE format.
     */
    String CRYPTO_KEY_V2 = "x-cos-key-v2";

    /** Initialization vector (IV) header that is used in the symmetric and envelope encryption mechanisms */
    String CRYPTO_IV = "x-cos-iv";

    /** JSON-encoded description of encryption materials used during encryption */
    String MATERIALS_DESCRIPTION = "x-cos-matdesc";

    /** Instruction file header to be placed in the metadata of instruction files */
    String CRYPTO_INSTRUCTION_FILE = "x-cos-crypto-instr-file";

    /** Header for the original, unencrypted size of an encrypted object */
    String UNENCRYPTED_CONTENT_LENGTH = "x-cos-unencrypted-content-length";

    /** Header for the optional original unencrypted Content MD5 of an encrypted object */
    String UNENCRYPTED_CONTENT_MD5 = "x-cos-unencrypted-content-md5";

    /**
     * Header in the request and response indicating the QCLOUD Key Management
     * System key id used for Server Side Encryption.
     */
    String SERVER_SIDE_ENCRYPTION_QCLOUD_KMS_KEYID = "x-cos-server-side-encryption-qcloud-kms-key-id";

    /** Header for optional redirect location of an object */
    String REDIRECT_LOCATION = "x-cos-website-redirect-location";

    /** Header for the optional restore information of an object */
    String RESTORE = "x-cos-restore";

    /** Header for the optional delete marker information of an object */
    String DELETE_MARKER = "x-cos-delete-marker";

    /**
     * Key wrapping algorithm such as "AESWrap" and "RSA/ECB/OAEPWithSHA-256AndMGF1Padding".
     */
    String CRYPTO_KEYWRAP_ALGORITHM = "x-cos-wrap-alg";
    /**
     * Content encryption algorithm, such as "AES/GCM/NoPadding".
     */
    String CRYPTO_CEK_ALGORITHM = "x-cos-cek-alg";
    /**
     * Tag length applicable to authenticated encrypt/decryption.
     */
    String CRYPTO_TAG_LENGTH = "x-cos-tag-len";

    /** Region where the bucket is located. This header is returned only in HEAD bucket and ListObjects response. */
    String COS_BUCKET_REGION = "x-cos-bucket-region";

}