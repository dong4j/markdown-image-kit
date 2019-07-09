package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

public interface ReadLimitInfo {
    /**
     * Returns the read limit for mark-and-reset during retries; or -1 if not
     * available.
     */
    int getReadLimit();
}