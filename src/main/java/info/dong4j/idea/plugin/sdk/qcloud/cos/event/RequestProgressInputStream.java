package info.dong4j.idea.plugin.sdk.qcloud.cos.event;

import java.io.*;

import static info.dong4j.idea.plugin.sdk.qcloud.cos.event.SDKProgressPublisher.publishRequestBytesTransferred;
import static info.dong4j.idea.plugin.sdk.qcloud.cos.event.SDKProgressPublisher.publishRequestReset;

/**
 * Used for request input stream progress tracking purposes.
 */
class RequestProgressInputStream extends ProgressInputStream {

    RequestProgressInputStream(InputStream is, ProgressListener listener) {
        super(is, listener);
    }

    @Override
    protected void onReset() {
        publishRequestReset(getListener(), getNotifiedByteCount());
    }

    @Override
    protected void onEOF() {
        onNotifyBytesRead();
    }

    @Override
    protected void onNotifyBytesRead() {
        publishRequestBytesTransferred(getListener(), getUnnotifiedByteCount());
    }
}