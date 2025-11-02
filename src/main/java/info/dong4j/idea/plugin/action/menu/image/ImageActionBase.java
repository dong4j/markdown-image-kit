package info.dong4j.idea.plugin.action.menu.image;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片操作基础类
 * <p>
 * 该类作为图片处理操作的基类，提供图片压缩、上传及 URL 处理的基础功能。主要用于支持图片相关操作的扩展，如图片压缩、上传至服务器并保存 URL 到剪贴板等。该类通过继承 AnAction 实现了 IntelliJ IDEA 的插件动作逻辑，支持在编辑器中触发图片处理操作。
 * <p>
 * 该类包含构建图片处理链、更新动作状态、执行图片处理动作等核心方法，并通过抽象方法定义了图标获取和链构建的接口，便于子类扩展。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public abstract class ImageActionBase extends AnAction {
    /**
     * 获取图标
     * <p>
     * 返回当前对象对应的图标
     *
     * @return 图标对象
     * @since 0.0.1
     */
    abstract protected Icon getIcon();

    /**
     * 构建处理链
     * <p>
     * 根据给定的事件和等待处理的流程映射，构建相应的处理链
     *
     * @param event             事件对象，包含操作上下文信息
     * @param waitingProcessMap 等待处理的流程映射，键为文档对象，值为Markdown图片列表
     * @since 0.0.1
     */
    abstract void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap);

    /**
     * 更新操作，用于启用或禁用该动作
     * <p>
     * 该方法通过设置动作可用状态为 true，使动作在 UI 中显示并可执行
     *
     * @param event 动作事件对象，包含执行动作所需的信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(true, event, this.getIcon(), ImageContents.IMAGE_TYPE_NAME);
    }

    /**
     * 处理用户触发的Action事件，用于构建Markdown图片处理链
     * <p>
     * 该方法根据当前选中的编辑器或文件夹，收集所有Markdown图片文件，并构建处理链
     * 如果是目录，使用多线程并行处理所有文件，最多使用15个线程，并显示处理进度
     *
     * @param event 事件对象，包含当前操作上下文信息
     */
    @SuppressWarnings("D")
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        log.trace("project's base path = {}", project.getBasePath());

        // 如果选中编辑器
        DataContext dataContext = event.getDataContext();
        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);

        if (null != editor) {
            // 单个文件直接处理
            VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (virtualFile != null) {
                Map<Document, List<MarkdownImage>> waitingProcessMap = new ConcurrentHashMap<>(64);
                this.buildWaitingProcessMap(waitingProcessMap, virtualFile);
                if (!waitingProcessMap.isEmpty()) {
                    this.buildChain(event, waitingProcessMap);
                }
            }
        } else {
            // 获取被选中的文件和目录
            VirtualFile[] virtualFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if (virtualFiles == null || virtualFiles.length == 0) {
                return;
            }

            // 收集需要处理的文件和目录
            List<VirtualFile> singleFiles = new ArrayList<>();
            List<VirtualFile> directories = new ArrayList<>();

            for (VirtualFile rootFile : virtualFiles) {
                if (ImageUtils.isImageFile(rootFile)) {
                    singleFiles.add(rootFile);
                } else if (rootFile.isDirectory()) {
                    directories.add(rootFile);
                }
            }

            // 如果没有需要处理的文件，直接返回
            if (singleFiles.isEmpty() && directories.isEmpty()) {
                return;
            }

            // 如果只有单个文件，直接处理
            if (singleFiles.size() == 1 && directories.isEmpty()) {
                Map<Document, List<MarkdownImage>> waitingProcessMap = new ConcurrentHashMap<>(64);
                this.buildWaitingProcessMap(waitingProcessMap, singleFiles.get(0));
                if (!waitingProcessMap.isEmpty()) {
                    this.buildChain(event, waitingProcessMap);
                }
                return;
            }

            // 使用后台任务收集文件（多线程处理目录）
            new FileCollectionTask(project, event, singleFiles, directories).queue();
        }
    }

    /**
     * 过滤文件
     * <p>
     * 判断给定的文件是否应该被处理。子类可以重写此方法实现自定义的过滤逻辑。
     * <p>
     * 例如：
     * <ul>
     *   <li>图片压缩时过滤掉 svg 和 gif 格式</li>
     *   <li>图片上传时过滤掉小于 1KB 或大于 5MB 的文件</li>
     * </ul>
     *
     * @param virtualFile 虚拟文件对象
     * @return 如果文件应该被处理返回 true，否则返回 false
     * @since 2.2.0
     */
    protected boolean shouldProcessFile(@NotNull VirtualFile virtualFile) {
        // 默认实现：处理所有文件
        return true;
    }

    /**
     * 构建等待处理的流程映射
     * <p>
     * 将给定的虚拟文件转换为 MarkdownImage 对象，并将其添加到等待处理的流程映射中。
     * 该方法线程安全，支持在多线程环境下调用。
     * <p>
     * 在添加文件前会调用 {@link #shouldProcessFile(VirtualFile)} 方法进行过滤。
     *
     * @param waitingProcessMap 等待处理的流程映射（应该是线程安全的）
     * @param virtualFile       虚拟文件对象
     */
    private void buildWaitingProcessMap(@NotNull Map<Document, List<MarkdownImage>> waitingProcessMap,
                                        @NotNull VirtualFile virtualFile) {
        // 调用子类的过滤方法
        if (!shouldProcessFile(virtualFile)) {
            log.trace("文件被过滤，跳过处理: {}", virtualFile.getPath());
            return;
        }

        MarkdownImage markdownImage = new MarkdownImage();
        markdownImage.setVirtualFile(virtualFile);
        markdownImage.setImageName(virtualFile.getName());
        markdownImage.setPath(virtualFile.getPath());
        try {
            markdownImage.setInputStream(virtualFile.getInputStream());
        } catch (IOException e) {
            log.trace("读取文件输入流失败: {}", virtualFile.getPath(), e);
            return;
        }
        markdownImage.setFilename(virtualFile.getName());
        markdownImage.setExtension(virtualFile.getExtension());
        markdownImage.setLocation(ImageLocationEnum.LOCAL);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);

        log.trace("添加图片到等待处理列表: {}", markdownImage.getFilename());
        // 使用线程安全的方式添加到映射中
        Document document = new DocumentImpl("");
        waitingProcessMap.computeIfAbsent(document, k -> Collections.synchronizedList(new ArrayList<>())).add(markdownImage);
    }

    /**
     * 获取动作更新线程
     *
     * <p>指定 update 方法在后台线程中执行，避免阻塞事件调度线程(EDT)。
     * 提高 UI 响应性，防止界面卡顿。
     *
     * @return ActionUpdateThread.BGT 后台线程
     * @see ActionUpdateThread#BGT
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 在后台线程中执行 update，避免阻塞 EDT
        return ActionUpdateThread.BGT;
    }

    /**
     * 文件收集任务类
     * <p>
     * 使用多线程并行收集文件，最多使用15个线程，并显示处理进度
     */
    @SuppressWarnings("D")
    private class FileCollectionTask extends Task.Backgroundable {
        /** 事件对象 */
        private final AnActionEvent event;
        /** 单个文件列表 */
        private final List<VirtualFile> singleFiles;
        /** 目录列表 */
        private final List<VirtualFile> directories;

        /**
         * 构造函数
         *
         * @param project     项目对象
         * @param event       事件对象
         * @param singleFiles 单个文件列表
         * @param directories 目录列表
         */
        FileCollectionTask(@Nullable Project project,
                           @NotNull AnActionEvent event,
                           @NotNull List<VirtualFile> singleFiles,
                           @NotNull List<VirtualFile> directories) {
            super(project, MikBundle.message("mik.action.collect.files"), true);
            this.event = event;
            this.singleFiles = singleFiles;
            this.directories = directories;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.pushState();
            indicator.setIndeterminate(false);

            try {
                Map<Document, List<MarkdownImage>> waitingProcessMap = new ConcurrentHashMap<>(64);

                // 处理单个文件
                if (!singleFiles.isEmpty()) {
                    for (VirtualFile file : singleFiles) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        indicator.setText2("处理文件: " + file.getName());
                        ImageActionBase.this.buildWaitingProcessMap(waitingProcessMap, file);
                    }
                }

                // 处理目录（多线程）
                if (!directories.isEmpty()) {
                    // 收集所有需要处理的文件
                    List<VirtualFile> allImageFiles = new ArrayList<>();
                    for (VirtualFile directory : directories) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        indicator.setText2("扫描目录: " + directory.getName());
                        List<VirtualFile> imageFiles = ImageUtils.recursivelyImageFile(directory);
                        allImageFiles.addAll(imageFiles);
                    }

                    int totalCount = allImageFiles.size();
                    if (totalCount > 0) {
                        // 动态计算线程池大小，最多使用15个线程
                        int threadPoolSize = Math.min(totalCount, 15);
                        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
                        log.trace("开始收集 {} 个文件，使用 {} 个线程", totalCount, threadPoolSize);

                        // 使用原子变量跟踪进度
                        AtomicInteger processedCount = new AtomicInteger(0);
                        List<CompletableFuture<?>> futures = new ArrayList<>();

                        // 为每个文件创建异步任务
                        for (VirtualFile imageFile : allImageFiles) {
                            if (indicator.isCanceled()) {
                                executorService.shutdownNow();
                                return;
                            }

                            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                                try {
                                    int currentProcessed = processedCount.incrementAndGet();
                                    String filename = imageFile.getName();

                                    // 更新进度（ProgressIndicator 是线程安全的）
                                    indicator.setText2(String.format("收集文件: %s (%d/%d)",
                                                                     filename, currentProcessed, totalCount));
                                    indicator.setFraction(currentProcessed * 1.0 / totalCount);

                                    // 处理文件
                                    ImageActionBase.this.buildWaitingProcessMap(waitingProcessMap, imageFile);
                                } catch (Exception e) {
                                    log.trace("处理文件时发生异常: {}", imageFile.getPath(), e);
                                }
                            }, executorService);

                            futures.add(future);
                        }

                        // 等待所有任务完成
                        try {
                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                            log.trace("文件收集完成，共处理 {} 个文件", totalCount);
                        } finally {
                            executorService.shutdown();
                        }
                    }
                }

                // 收集完成后调用 buildChain
                if (!waitingProcessMap.isEmpty()) {
                    // 在 EDT 线程中执行 buildChain
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        ImageActionBase.this.buildChain(this.event, waitingProcessMap);
                    });
                }
            } finally {
                indicator.setFraction(1.0);
                indicator.popState();
            }
        }
    }
}
