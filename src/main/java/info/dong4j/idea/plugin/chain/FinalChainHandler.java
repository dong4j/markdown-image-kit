package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public class FinalChainHandler extends ActionHandlerAdapter {
    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.final.title");
    }

    /**
     * Is enabled
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isRename();
    }

    /**
     * 根据配置重新设置 imageName
     *
     * @param data the data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        Map<Document, List<MarkdownImage>> processededData = data.getWaitingProcessMap();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : processededData.entrySet()) {
            List<MarkdownImage> markdownImages = imageEntry.getValue();
            for (MarkdownImage markdownImage : markdownImages) {

                if (markdownImage.getInputStream() != null) {
                    try {
                        markdownImage.getInputStream().close();
                    } catch (IOException ignored) {
                    }
                }
            }
            markdownImages.clear();
        }
        processededData.clear();
        return true;
    }
}
