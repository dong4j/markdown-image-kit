package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import info.dong4j.idea.plugin.sdk.qcloud.cos.COS;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PartETag;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.UploadPartRequest;

import java.util.concurrent.Callable;

public class UploadPartCallable implements Callable<PartETag> {
    private final COS cos;
    private final UploadPartRequest request;

    public UploadPartCallable(COS cos, UploadPartRequest request) {
        this.cos = cos;
        this.request = request;
    }

    public PartETag call() throws Exception {
        PartETag partETag = cos.uploadPart(request).getPartETag();
        return partETag;
    }
}
