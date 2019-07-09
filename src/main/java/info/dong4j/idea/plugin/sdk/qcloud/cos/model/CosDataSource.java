package info.dong4j.idea.plugin.sdk.qcloud.cos.model;

import org.slf4j.Logger;

import java.io.*;

import static info.dong4j.idea.plugin.sdk.qcloud.cos.utils.IOUtils.release;


/**
 * Used to represent an COS data source that either has a file or an input
 * stream.
 */
public interface CosDataSource {
    File getFile();
    void setFile(File file);
    InputStream getInputStream();
    void setInputStream(InputStream inputStream);

    /**
     * {@link CosDataSource} specific utilities.
     */
    enum Utils {
        ;
        /**
         * Clean up any temporary streams created during the execution,
         * and restore the original file and/or input stream.
         */
        public static void cleanupDataSource(CosDataSource req,
                final File fileOrig, final InputStream inputStreamOrig,
                InputStream inputStreamCurr, Logger log) {
            if (fileOrig != null) {
                // We opened a file underneath so would need to release it
                release(inputStreamCurr, log);
            }
            // restore the original input stream so the caller could close
            // it if necessary
            req.setInputStream(inputStreamOrig);
            req.setFile(fileOrig);
        }
    }
}
