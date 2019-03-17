package info.dong4j.idea.plugin.action;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.util.AliyunUploadUtils;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到阿里 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-12 17:20
 */
@Slf4j
public final class AliyunObjectStorageServiceAction extends AbstractObjectStorageServiceAction {
    /** 是否有上传的任务*/
    private static boolean uploading = false;

    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        return OssPersistenConfig.getInstance().getState().getAliyunOssState().isPassedTest();
    }

    /**
     * 处理点击 "upload to Aliyun OSS" 按钮的逻辑
     *
     * @param anActionEvent the an action event
     */
    // @Override
    // public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    //     Project project = anActionEvent.getProject();
    //     if (project != null) {
    //         log.trace("project's base path = {}", project.getBasePath());
    //         // 获取当前操作的文件
    //         PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
    //         if (psiFile != null && isMardownFile(psiFile.getOriginalFile().getName())) {
    //             // 解析 markdown 中的图片标签
    //             Document document = psiFile.getViewProvider().getDocument();
    //             String text = Objects.requireNonNull(document).getText();
    //
    //             // 用回车键来分隔几个元素
    //             String[] textArray = text.split("\n");
    //             String url = "";
    //             for (int i = 0; i < textArray.length; i++) {
    //                 // todo-dong4j : (2019年03月12日 19:02) [要求只是是图片表示, 则肯定是以 ![]() 的形式出现]
    //                 if (StringUtils.isNotBlank(textArray[i]) && textArray[i].trim().startsWith("![") && textArray[i].trim().endsWith(")")) {
    //                     log.trace(textArray[i]);
    //                     // 替换字符串
    //                     Map<String, String> map = ParserUtils.parseImageTag(textArray[i]);
    //                     for (Map.Entry<String, String> result : map.entrySet()) {
    //                         log.trace("key = {}, value = {}", result.getKey(), result.getValue());
    //                         // 上传到 OSS
    //                         url = upload(anActionEvent, result.getValue());
    //                         textArray[i] = ParserUtils.parse0(HTML_TAG_EXTEND, result.getKey(), url, result.getKey(), url);
    //                     }
    //                 }
    //             }
    //
    //             // 替换全部字符串
    //             StringBuilder stringBuilder = new StringBuilder();
    //             for (String string : textArray) {
    //                 stringBuilder.append(string).append("\n");
    //             }
    //             PsiDocumentUtils.commitAndSaveDocument(project, document, stringBuilder.toString());
    //             // notifucation(url);
    //         }
    //     }
    // }

    private String upload(AnActionEvent anActionEvent, String filePath) {
        final Project project = anActionEvent.getProject();
        PsiFile psiFile = findImageResource(project, filePath);
        if (psiFile != null) {
            filePath = psiFile.getVirtualFile().getPath();
            String name = AliyunUploadUtils.uploadImg2Oss(new File(filePath));
            return AliyunUploadUtils.getUrl(name);
        }
        return filePath;
    }

    @Contract(pure = true)
    @Override
    protected void upload(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingForUploadImages) {
        // todo-dong4j : (2019年03月15日 19:06) []
        //  1. 是否设置图片压缩
        //  2. 是否开启图床迁移
        //  3. 是否开启备份
        final Project project = event.getProject();
        if(project != null){
            for(Map.Entry<Document, List<MarkdownImage>> entry : waitingForUploadImages.entrySet()){
                Document document = entry.getKey();
                for (MarkdownImage markdownImage : entry.getValue()) {
                    if(markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)){
                        String imageName = markdownImage.getPath();
                        if(StringUtils.isNotBlank(imageName)){
                            Collection<VirtualFile> findedFiles = FilenameIndex.getVirtualFilesByName(project, imageName, GlobalSearchScope.allScope(project));
                            if (findedFiles.size() <= 0) {
                                notifucation("upload error", "", "Could not find 「" + imageName +  "」 in project", NotificationType.ERROR);
                                continue;
                            }
                            // todo-dong4j : (2019年03月15日 20:14) [此操作耗时, 放入异步处理]
                            for(VirtualFile file : findedFiles){
                                String name = AliyunUploadUtils.uploadImg2Oss(new File(file.getPath()));
                                String uploadedUrl = AliyunUploadUtils.getUrl(name);
                                markdownImage.setUploadedUrl(uploadedUrl);
                                // 只取第一个图片,
                                break;
                            }
                        }
                    }
                    // todo-dong4j : (2019年03月15日 20:02) [此处会多次修改, 考虑直接使用 setText() 一次性修改全部文本数据]
                    PsiDocumentUtils.commitAndSaveDocument(project, document, markdownImage);
                }
            }
        }
    }


    /**
     * 通过文件名查找文件
     *
     * @param project  the project
     * @param filePath the file path
     * @return the psi file
     */
    @Nullable
    private static PsiFile findImageResource(Project project, String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf(File.pathSeparator) + 1);
        // 在整个 project 范围内查找
        PsiFile[] foundFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
        if (foundFiles.length <= 0) {
            return null;
        }
        return foundFiles[0];
    }
}
