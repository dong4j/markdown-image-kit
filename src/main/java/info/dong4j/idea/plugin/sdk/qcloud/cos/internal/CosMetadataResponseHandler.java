package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.http.CosHttpResponse;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;

public class CosMetadataResponseHandler extends AbstractCosResponseHandler<ObjectMetadata>{

    @Override
    public CosServiceResponse<ObjectMetadata> handle(CosHttpResponse response) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        populateObjectMetadata(response, metadata);

        CosServiceResponse<ObjectMetadata> cosResponse = parseResponseMetadata(response);
        cosResponse.setResult(metadata);
        return cosResponse;
    }

}
