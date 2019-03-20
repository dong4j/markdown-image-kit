// package info.dong4j.idea.plugin.strategy;
//
// import com.qiniu.storage.Configuration;
// import com.qiniu.storage.UploadManager;
// import com.qiniu.util.Auth;
//
// import info.dong4j.idea.plugin.enums.ZoneEnum;
// import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
// import info.dong4j.idea.plugin.settings.ImageManagerState;
// import info.dong4j.idea.plugin.settings.OssState;
// import info.dong4j.idea.plugin.settings.QiniuOssState;
// import info.dong4j.idea.plugin.singleton.QiniuOssClient;
// import info.dong4j.idea.plugin.util.DES;
// import info.dong4j.idea.plugin.util.EnumsUtils;
//
// import org.apache.commons.lang.StringUtils;
// import org.jetbrains.annotations.Contract;
// import org.jetbrains.annotations.NotNull;
//
// import java.io.*;
// import java.util.Map;
// import java.util.Optional;
//
// import javax.swing.JPanel;
//
// import lombok.extern.slf4j.Slf4j;
//
// /**
//  * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
//  * <p>Description: </p>
//  *
//  * @author dong4j
//  * @email sjdong3 @iflytek.com
//  * @since 2019 -03-19 15:24
//  */
// @Slf4j
// public class QiniuUploadStrategy implements UploadStrategy {
//     private String endpoint;
//     private String accessKey;
//     private String secretKey;
//     private String bucketName;
//     private int zoneIndex;
//
//     private QiniuOssState qiniuOssState = ImageManagerPersistenComponent.getInstance().getState().getQiniuOssState();
//
//     @Override
//     public String upload(InputStream inputStream, String fileName) {
//         return uploadFromPaste(inputStream, fileName);
//     }
//
//     /**
//      * Upload from state string.
//      *
//      * @param inputStream the input stream
//      * @param fileName    the file name
//      * @return the string
//      */
//     @NotNull
//     private String uploadFromPaste(InputStream inputStream, String fileName) {
//         endpoint = qiniuOssState.getEndpoint();
//         accessKey = qiniuOssState.getAccessKey();
//         secretKey = DES.decrypt(qiniuOssState.getAccessSecretKey(), ImageManagerState.QINIU);
//         bucketName = qiniuOssState.getBucketName();
//         zoneIndex = qiniuOssState.getZoneIndex();
//
//         return upload(inputStream,
//                       fileName,
//                       bucketName,
//                       accessKey,
//                       secretKey,
//                       endpoint,
//                       zoneIndex,
//                       UploadWayEnum.FROM_PASTE);
//     }
//
//     /**
//      * Upload from test string.
//      * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage #upload}
//      *
//      * @param inputStream the input stream
//      * @param fileName    the file name
//      * @param jPanel      the j panel
//      * @return the string
//      */
//     public String uploadFromTest(InputStream inputStream, String fileName, JPanel jPanel) {
//         Map<String, String> map = getTestFieldText(jPanel);
//         zoneIndex = Integer.parseInt(map.get("zoneIndex"));
//         bucketName = map.get("bucketName");
//         accessKey = map.get("accessKey");
//         secretKey = map.get("secretKey");
//         endpoint = map.get("domain");
//
//         return upload(inputStream,
//                       fileName,
//                       bucketName,
//                       accessKey,
//                       secretKey,
//                       endpoint,
//                       zoneIndex,
//                       UploadWayEnum.FROM_TEST);
//     }
//
//     /**
//      * Upload string.
//      *
//      * @param inputStream   the input stream
//      * @param fileName      the file name
//      * @param bucketName    the bucketName name
//      * @param accessKey     the access key
//      * @param secretKey     the secret key
//      * @param endpoint      the endpoint
//      * @param zoneIndex     the zone index
//      * @param uploadWayEnum the upload way enum
//      * @return the string
//      */
//     @NotNull
//     @Contract(pure = true)
//     public String upload(InputStream inputStream,
//                          String fileName,
//                          String bucketName,
//                          String accessKey,
//                          String secretKey,
//                          String endpoint,
//                          int zoneIndex,
//                          UploadWayEnum uploadWayEnum) {
//
//         String url;
//         QiniuOssClient qiniuOssClient = QiniuOssClient.getInstance();
//         if (uploadWayEnum.equals(UploadWayEnum.FROM_TEST)) {
//             Optional<ZoneEnum> zone = EnumsUtils.getEnumObject(ZoneEnum.class, e -> e.getIndex() == zoneIndex);
//             Configuration cfg = new Configuration(zone.orElse(ZoneEnum.EAST_CHINA).zone);
//             UploadManager ossClient = new UploadManager(cfg);
//             Auth auth = Auth.create(accessKey, secretKey);
//             QiniuOssClient.buildToken(auth, bucketName);
//
//             qiniuOssClient.setDomain(endpoint);
//             url = qiniuOssClient.upload(ossClient, inputStream, fileName);
//
//             if (StringUtils.isNotBlank(url)) {
//                 int hashcode = bucketName.hashCode() +
//                                accessKey.hashCode() +
//                                secretKey.hashCode() +
//                                endpoint.hashCode() +
//                                zoneIndex;
//                 OssState.saveStatus(qiniuOssState, hashcode, ImageManagerState.OLD_HASH_KEY);
//                 qiniuOssClient.setOssClient(ossClient);
//             }
//         } else {
//             url = qiniuOssClient.upload(inputStream, fileName);
//         }
//         return url;
//     }
// }
