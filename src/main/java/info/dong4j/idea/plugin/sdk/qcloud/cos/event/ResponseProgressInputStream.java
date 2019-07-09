package info.dong4j.idea.plugin.sdk.qcloud.cos.event;

import java.io.*;

import static info.dong4j.idea.plugin.sdk.qcloud.cos.event.SDKProgressPublisher.publishResponseBytesTransferred;
import static info.dong4j.idea.plugin.sdk.qcloud.cos.event.SDKProgressPublisher.publishResponseReset;

/**
 * Used for response input stream progress tracking purposes.
 */
class ResponseProgressInputStream extends ProgressInputStream {
    ResponseProgressInputStream(InputStream is, ProgressListener listener) {
        super(is, listener);
    }

    @Override
    protected void onReset() {
        publishResponseReset(getListener(), getNotifiedByteCount());
    }

    @Override
    protected void onEOF() {
        onNotifyBytesRead();
    }

    @Override
    protected void onNotifyBytesRead() {
        publishResponseBytesTransferred(getListener(), getUnnotifiedByteCount());
    }
}