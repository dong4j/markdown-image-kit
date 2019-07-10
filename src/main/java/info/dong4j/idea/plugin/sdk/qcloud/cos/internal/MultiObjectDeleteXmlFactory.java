package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.DeleteObjectsRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.DeleteObjectsRequest.KeyVersion;

/**
 * Factory for creating XML fragments from {@link DeleteObjectsRequest} objects
 * that can be sent to COS.
 */
public class MultiObjectDeleteXmlFactory {

    /**
     * Converts the specified {@link DeleteObjectsRequest} object to an XML fragment that
     * can be sent to Qcloud COS.
     *
     * @param rq
     *            The {@link DeleteObjectsRequest}
     */
    public byte[] convertToXmlByteArray(DeleteObjectsRequest rq) throws CosClientException {
        
        XmlWriter xml = new XmlWriter();
        xml.start("Delete");
        if ( rq.getQuiet() ) {
            xml.start("Quiet").value("true").end();
        }
        
        for (KeyVersion keyVersion : rq.getKeys()) {
            writeKeyVersion(xml, keyVersion);
        }

        xml.end();

        return xml.getBytes();
    }

    private void writeKeyVersion(XmlWriter xml, KeyVersion keyVersion) {
        xml.start("Object");
        xml.start("Key").value(keyVersion.getKey()).end();
        if (keyVersion.getVersion() != null) {
            xml.start("VersionId").value(keyVersion.getVersion()).end();
        }
        xml.end();
    }

}