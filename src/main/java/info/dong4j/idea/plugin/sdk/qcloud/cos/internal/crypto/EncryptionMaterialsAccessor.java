package info.dong4j.idea.plugin.sdk.qcloud.cos.internal.crypto;

import java.util.Map;

/**
 * Retrieves encryption materials from some source.
 */
public interface EncryptionMaterialsAccessor {

    /**
     * Retrieves encryption materials matching the specified description from some source.
     *
     * @param materialsDescription
     *      A Map<String, String> whose key-value pairs describe an encryption materials object
     * @return
     *      The encryption materials that match the description, or null if no matching encryption materials found.
     */
    EncryptionMaterials getEncryptionMaterials(Map<String, String> materialsDescription);
}