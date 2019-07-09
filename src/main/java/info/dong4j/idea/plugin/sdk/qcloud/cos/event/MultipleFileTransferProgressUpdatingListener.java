package info.dong4j.idea.plugin.sdk.qcloud.cos.event;

import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.TransferProgress;

/**
 * TransferProgressUpdatingListener for multiple-file transfer. In addition
 * to updating the TransferProgress, it also sends out ByteTrasnferred
 * events to a ProgressListenerChain.
 */
public final class MultipleFileTransferProgressUpdatingListener extends
        TransferProgressUpdatingListener implements DeliveryMode {
    private final ProgressListenerChain progressListenerChain;

    public MultipleFileTransferProgressUpdatingListener(
            TransferProgress transferProgress,
            ProgressListenerChain progressListenerChain) {
        super(transferProgress);
        this.progressListenerChain = progressListenerChain;
    }

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        super.progressChanged(progressEvent);
        progressListenerChain.progressChanged(progressEvent);
    }

    @Override
    public boolean isSyncCallSafe() {
        return progressListenerChain == null
            || progressListenerChain.isSyncCallSafe();
    }
}