package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

public interface VIDResult {

    /**
     * get requestid for this upload
     *
     * @return requestid
     */
    String getRequestId();

    /**
     * set requestId for this upload
     *
     * @param requestId the requestId for the upload
     */

    void setRequestId(String requestId);

    /**
     * get date header for this upload
     *
     * @return date str
     */
    String getDateStr();

    /**
     * set date str for this upload
     *
     * @param dateStr date str header
     */
    void setDateStr(String dateStr);
}
