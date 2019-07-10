package info.dong4j.idea.plugin.sdk.qcloud.cos.event;

import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.Transfer;
import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.Transfer.TransferState;

/**
 * Listener for transfer state changes.  Not intended to be consumed externally.
 */
public interface TransferStateChangeListener {
    void transferStateChanged(Transfer transfer, TransferState state);
}