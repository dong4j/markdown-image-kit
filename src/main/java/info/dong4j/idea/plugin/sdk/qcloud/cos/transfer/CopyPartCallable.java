package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import info.dong4j.idea.plugin.sdk.qcloud.cos.COS;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyPartRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.CopyPartResult;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PartETag;

import java.util.concurrent.Callable;

/**
 * An implementation of the Callable interface responsible for carrying out the
 * Copy part requests.
 *
 */
public class CopyPartCallable implements Callable<PartETag> {

    /** Reference to the COS client object used for initiating copy part request.*/
    private final COS cos;
    /** Copy part request to be initiated.*/
    private final CopyPartRequest request;

    public CopyPartCallable(COS cos, CopyPartRequest request) {
        this.cos = cos;
        this.request = request;
    }

    public PartETag call() throws Exception {
        CopyPartResult copyPartResult = cos.copyPart(request);
        return copyPartResult == null ? null : copyPartResult.getPartETag();
    }
}