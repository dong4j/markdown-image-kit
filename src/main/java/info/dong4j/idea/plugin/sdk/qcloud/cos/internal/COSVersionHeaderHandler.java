package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.Headers;
import info.dong4j.idea.plugin.sdk.qcloud.cos.http.CosHttpResponse;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.XmlResponsesSaxParser.CopyObjectResultHandler;

/**
 * Header handler to pull the COS_VERSION_ID header out of the response. This
 * header is required for the copyPart and copyObject api methods.
 */
public class COSVersionHeaderHandler implements HeaderHandler<CopyObjectResultHandler> {

    @Override
    public void handle(CopyObjectResultHandler result, CosHttpResponse response) {
        result.setVersionId(response.getHeaders().get(Headers.COS_VERSION_ID));
    }
}