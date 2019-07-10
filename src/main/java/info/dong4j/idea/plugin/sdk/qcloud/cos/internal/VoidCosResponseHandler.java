package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.http.CosHttpResponse;

public class VoidCosResponseHandler extends AbstractCosResponseHandler<Void> {

    @Override
    public CosServiceResponse<Void> handle(CosHttpResponse response) throws Exception {
        CosServiceResponse<Void> csp = new CosServiceResponse<>();
        return csp;
    }

}
