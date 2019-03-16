package info.dong4j.idea.plugin.strategy;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-17 02:10
 * @email sjdong3@iflytek.com
 */
public interface UploadStrategy {
    String upload(InputStream inputStream, String fileName);
}
