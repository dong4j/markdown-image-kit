package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.http.CosHttpResponse;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObject;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObjectInputStream;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;

public class COSObjectResponseHandler extends AbstractCosResponseHandler<COSObject> {

    @Override
    public CosServiceResponse<COSObject> handle(CosHttpResponse response) throws Exception {
        COSObject object = new COSObject();
        CosServiceResponse<COSObject> cosResponse = parseResponseMetadata(response);

        ObjectMetadata metadata = object.getObjectMetadata();
        populateObjectMetadata(response, metadata);
        object.setObjectContent(
                new COSObjectInputStream(response.getContent(), response.getHttpRequest()));
        cosResponse.setResult(object);
        return cosResponse;
    }
    
    @Override
    public boolean needsConnectionLeftOpen() {
        return true;
    }

}
