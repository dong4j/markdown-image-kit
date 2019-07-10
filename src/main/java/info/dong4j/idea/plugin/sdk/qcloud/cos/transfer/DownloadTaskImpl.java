package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import info.dong4j.idea.plugin.sdk.qcloud.cos.COS;
import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.SkipMd5CheckStrategy;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSEncryption;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.COSObject;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.GetObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.utils.ServiceUtils;

final class DownloadTaskImpl implements ServiceUtils.RetryableCOSDownloadTask {
    private final COS cos;
    private final DownloadImpl download;
    private final GetObjectRequest getObjectRequest;
    private final SkipMd5CheckStrategy skipMd5CheckStrategy = SkipMd5CheckStrategy.INSTANCE;

    DownloadTaskImpl(COS cos, DownloadImpl download, GetObjectRequest getObjectRequest) {
        this.cos = cos;
        this.download = download;
        this.getObjectRequest = getObjectRequest;
    }
    
    @Override
    public COSObject getCOSObjectStream() {
        COSObject cosObject = cos.getObject(getObjectRequest);
        download.setCosObject(cosObject);
        return cosObject;
    }

    @Override
    public boolean needIntegrityCheck() {
        // Don't perform the integrity check if the checksum won't matchup.
        return !(cos instanceof COSEncryption)
                && !skipMd5CheckStrategy.skipClientSideValidationPerRequest(getObjectRequest);
    }
}
