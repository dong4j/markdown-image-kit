package info.dong4j.idea.plugin.sdk.qcloud.cos.model;

/**
 * Implemented by classes that support the option of using SSE with COS Key
 * Management System.
 */
public interface SSECOSKeyManagementParamsProvider {
    /**
     * Returns the optional SSECOSKeyManagementParams to use to encrypt the
     * uploaded object.
     *
     * @return The optional SSECOSKeyManagementParams to use to encrypt the
     *         uploaded object.
     */
    SSECOSKeyManagementParams getSSECOSKeyManagementParams();
}