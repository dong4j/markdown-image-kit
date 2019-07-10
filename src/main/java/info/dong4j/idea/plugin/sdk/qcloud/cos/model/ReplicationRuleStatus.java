package info.dong4j.idea.plugin.sdk.qcloud.cos.model;

/**
 * A enum class for status of a QCloud bucket replication rule.
 */
public enum ReplicationRuleStatus {
    Enabled("Enabled"),

    Disabled("Disabled");

    private final String status;

    ReplicationRuleStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}