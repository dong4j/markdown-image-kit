package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.Headers;
import info.dong4j.idea.plugin.sdk.qcloud.cos.http.CosHttpResponse;

import java.util.Map;

public class VIDResultHandler<T extends VIDResult> implements HeaderHandler<T> {
    @Override
    public void handle(T result, CosHttpResponse response) {
        Map<String, String> responseHeaderMap = response.getHeaders();
        result.setRequestId(responseHeaderMap.get(Headers.REQUEST_ID));
        result.setDateStr(responseHeaderMap.get(Headers.DATE));
    }
}
