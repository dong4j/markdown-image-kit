package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

public interface Unmarshaller<T, R> {

    T unmarshall(R in) throws Exception;

}
