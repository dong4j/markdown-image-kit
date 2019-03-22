package info.dong4j.idea.plugin.watch;

import com.intellij.openapi.editor.Editor;

import org.jetbrains.annotations.NotNull;

import java.awt.Image;
import java.util.Map;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-22 18:38
 */
public abstract class PasteActionHandler extends BaseActionHandler {
    protected Editor editor;
    Map<String, Image> imageMap;

    PasteActionHandler(@NotNull Editor editor, Map<String, Image> imageMap) {
        this.editor = editor;
        this.imageMap = imageMap;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean execute() {
        return false;
    }
}
