package info.dong4j.idea.plugin.sdk.qcloud.cos.utils;

interface Codec {
    byte[] encode(byte[] src);

    byte[] decode(byte[] src, final int length);
}
