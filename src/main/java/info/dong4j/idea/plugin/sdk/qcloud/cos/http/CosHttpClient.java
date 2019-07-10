package info.dong4j.idea.plugin.sdk.qcloud.cos.http;

import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.CosServiceRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.CosServiceResponse;

public interface CosHttpClient {
    <X, Y extends CosServiceRequest> X exeute(CosHttpRequest<Y> request,
                                              HttpResponseHandler<CosServiceResponse<X>> responseHandler)
                    throws CosClientException;

    void shutdown();
}
