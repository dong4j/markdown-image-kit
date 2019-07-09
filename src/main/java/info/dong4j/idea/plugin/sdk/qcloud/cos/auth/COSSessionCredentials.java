package info.dong4j.idea.plugin.sdk.qcloud.cos.auth;

public interface COSSessionCredentials extends COSCredentials {

    /**
     * Returns the session token for this session.
     */
    String getSessionToken();
}
