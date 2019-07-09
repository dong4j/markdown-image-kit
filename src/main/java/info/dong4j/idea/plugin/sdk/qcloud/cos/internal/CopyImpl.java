package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.event.ProgressListenerChain;
import info.dong4j.idea.plugin.sdk.qcloud.cos.event.TransferStateChangeListener;
import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosServiceException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.AbstractTransfer;
import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.Copy;
import info.dong4j.idea.plugin.sdk.qcloud.cos.transfer.TransferProgress;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * An implementation of the Copy Interface that helps in retrieving the result
 * of the copy operation.
 */
public class CopyImpl extends AbstractTransfer implements Copy {

    public CopyImpl(String description, TransferProgress transferProgress,
            ProgressListenerChain progressListenerChain,
            TransferStateChangeListener stateChangeListener) {
        super(description, transferProgress, progressListenerChain,
                stateChangeListener);
    }

    /**
     * Waits for this copy operation to complete and returns the result of the
     * operation. Be prepared to handle errors when calling this method. Any
     * errors that occurred during the asynchronous transfer will be re-thrown
     * through this method.
     *
     * @return The result of this transfer.
     *
     * @throws CosClientException If any errors are encountered in the client while making the
     *         request or handling the response.
     *
     * @throws CosServiceException If any errors occurred in COS while processing the request.
     *
     * @throws InterruptedException
     *             If this thread is interrupted while waiting for the upload to
     *             complete.
     */
    public CopyResult waitForCopyResult() throws CosClientException,
            CosServiceException, InterruptedException {
        try {
            CopyResult result = null;
            while (!monitor.isDone() || result == null) {
                Future<?> f = monitor.getFuture();
                result = (CopyResult) f.get();
            }
            return result;
        } catch (ExecutionException e) {
            rethrowExecutionException(e);
            return null;
        }
    }
}
