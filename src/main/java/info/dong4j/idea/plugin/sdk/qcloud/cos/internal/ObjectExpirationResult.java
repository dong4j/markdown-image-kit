package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import java.util.Date;


/**
 * Interface for service responses that receive the x-cos-expiration header.
 *
 * @see Headers#EXPIRATION
 */
public interface ObjectExpirationResult {

    /**
     * Returns the expiration date of the object, or null if the object is not
     * configured to expire.
     */
    Date getExpirationTime();

    /**
     * Sets the expiration date of the object.
     *
     * @param expiration
     *            The date the object will expire.
     */
    void setExpirationTime(Date expiration);

    /**
     * Returns the bucket lifecycle configuration rule ID for the expiration of
     * this object.
     *
     * @see Rule#getId()
     */
    String getExpirationTimeRuleId();

    /**
     * Sets the bucket lifecycle configuration rule ID for the expiration of
     * this object.
     *
     * @param ruleId
     *            The rule ID of this object's expiration configuration
     */
    void setExpirationTimeRuleId(String ruleId);

}
