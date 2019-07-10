package info.dong4j.idea.plugin.sdk.qcloud.cos.model;

import info.dong4j.idea.plugin.sdk.qcloud.cos.Headers;
import info.dong4j.idea.plugin.sdk.qcloud.cos.http.CosHttpResponse;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.AbstractCosResponseHandler;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.CosServiceResponse;

public class HeadBucketResultHandler extends AbstractCosResponseHandler<HeadBucketResult> {

    @Override
    public CosServiceResponse<HeadBucketResult> handle(CosHttpResponse response)
            throws Exception {
        final CosServiceResponse<HeadBucketResult> cosResponse = new CosServiceResponse<HeadBucketResult>();
        final HeadBucketResult result = new HeadBucketResult();
        result.setBucketRegion(response.getHeaders().get(Headers.COS_BUCKET_REGION));
        cosResponse.setResult(result);
        return cosResponse;
    }
}