package info.dong4j.idea.plugin.sdk.qcloud.cos.event;

import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.TransferProgress;

public class TransferProgressUpdatingListener extends SyncProgressListener {
    private final TransferProgress transferProgress;

    public TransferProgressUpdatingListener(TransferProgress transferProgress) {
        this.transferProgress = transferProgress;
    }

    public void progressChanged(ProgressEvent progressEvent) {
        long bytes = progressEvent.getBytesTransferred();
        if (bytes == 0)
            return; // only interested in non-zero bytes
        transferProgress.updateProgress(bytes);
    }
}