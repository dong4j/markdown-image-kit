package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.util.io.FileUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.FileType;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * 下载图片处理类
 * <p>
 * 用于处理网络图片的下载操作，将网络图片下载到本地并转换为输入流。
 * 该类继承自 ActionHandlerAdapter，用于在特定事件触发时执行图片下载逻辑。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 1.0.0
 */
@Slf4j
public class DownloadImageHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示操作进度标题
     *
     * @return 名称字符串
     * @since 1.0.0
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.download.title");
    }

    /**
     * 判断当前状态是否启用下载功能
     * <p>
     * 根据当前状态判断是否启用下载功能，只有 location 为 NETWORK 的图片才需要下载
     *
     * @param data 事件数据，用于上下文信息
     * @return 是否启用下载功能
     * @since 1.0.0
     */
    @Override
    public boolean isEnabled(EventData data) {
        return IntentionActionBase.getState().isApplyToNetworkImages();
    }

    /**
     * 处理Markdown图片数据，下载网络图片并转换为输入流
     * <p>
     * 该方法用于处理Markdown图片数据，首先检查图片的 location 是否为 NETWORK。
     * 如果是网络图片，则下载图片并转换为输入流，同时将 location 设置为 LOCAL。
     * 如果下载失败，则从迭代器中移除该图片。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历和移除图片
     * @param markdownImage Markdown图片对象，包含图片路径和输入流
     * @since 1.0.0
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();

        // 只处理网络图片
        if (!ImageLocationEnum.NETWORK.equals(markdownImage.getLocation())) {
            return;
        }

        String imageUrl = markdownImage.getPath();
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.trace("图片URL为空, remove markdownImage = {}", markdownImage);
            imageIterator.remove();
            return;
        }

        try {
            // 下载图片
            URLConnection connection = getUrlConnection(imageUrl);

            // 从 HTTP 响应头获取 Content-Type
            String contentType = connection.getContentType();
            String extension = getExtensionFromContentType(contentType);

            byte[] imageBytes;
            try (InputStream in = connection.getInputStream()) {
                imageBytes = FileUtil.loadBytes(in);
            }

            if (imageBytes == null || imageBytes.length == 0) {
                log.trace("下载图片为空, remove markdownImage = {}", markdownImage);
                imageIterator.remove();
                return;
            }

            // 如果没有从 Content-Type 获取到扩展名，尝试从文件头推断
            if (extension == null || extension.isEmpty()) {
                extension = getExtensionFromFileHeader(imageBytes);
            }

            // 如果仍然无法推断，使用默认扩展名
            if (extension == null || extension.isEmpty()) {
                extension = ".png";
                log.trace("无法推断图片类型，使用默认扩展名: {}", extension);
            }

            // 更新图片名称和扩展名
            String originalImageName = markdownImage.getImageName();
            String newImageName = updateImageNameWithExtension(originalImageName, extension);
            markdownImage.setImageName(newImageName);
            markdownImage.setExtension(extension.startsWith(".") ? extension.substring(1) : extension);

            // 设置输入流
            markdownImage.setInputStream(new ByteArrayInputStream(imageBytes));
            // 将 location 设置为 LOCAL，以便后续流程可以继续处理
            markdownImage.setLocation(ImageLocationEnum.LOCAL);

            log.debug("下载图片成功: {} -> {} bytes, extension: {}", imageUrl, imageBytes.length, extension);
        } catch (IOException e) {
            log.error("下载图片失败: {}", imageUrl, e);
            imageIterator.remove();
        }
    }

    /**
     * 根据图片URL获取 URL 连接对象
     * <p>
     * 通过解析图片URL生成 URL 对象，并创建对应的 URL 连接。
     * 设置连接和读取超时时间，用于后续的网络请求操作。
     *
     * @param imageUrl 图片URL地址
     * @return 返回配置好的 URL 连接对象
     * @throws IOException 当路径解析或 URL 创建失败时抛出
     * @since 1.0.0
     */
    @NotNull
    private URLConnection getUrlConnection(@NotNull String imageUrl) throws IOException {
        URL url;
        try {
            url = new URI(imageUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("无效的URL: " + imageUrl, e);
        }
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(5000);
        return connection;
    }

    /**
     * 从 Content-Type 获取文件扩展名
     * <p>
     * 根据 HTTP 响应头的 Content-Type 推断图片文件扩展名。
     * 支持的常见图片类型：image/jpeg, image/png, image/gif, image/webp 等。
     *
     * @param contentType HTTP 响应头的 Content-Type，可能包含 charset 等信息
     * @return 文件扩展名（带点号，如 ".jpg"），如果无法推断则返回 null
     * @since 1.0.0
     */
    @Nullable
    private String getExtensionFromContentType(@Nullable String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }

        // Content-Type 可能包含 charset 等信息，例如 "image/jpeg; charset=utf-8"
        String mimeType = contentType.split(";")[0].trim().toLowerCase();

        return switch (mimeType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            case "image/svg+xml" -> ".svg";
            case "image/tiff" -> ".tiff";
            case "image/x-icon", "image/vnd.microsoft.icon" -> ".ico";
            case "image/avif" -> ".avif";
            case "image/heic" -> ".heic";
            case "image/heif" -> ".heif";
            default -> null;
        };
    }

    /**
     * 从文件头（magic bytes）推断文件扩展名
     * <p>
     * 通过读取图片文件的前几个字节（文件头）来判断图片类型，这种方法比依赖 Content-Type 更可靠。
     *
     * @param imageBytes 图片文件的字节数组
     * @return 文件扩展名（带点号，如 ".jpg"），如果无法推断则返回 null
     * @since 1.0.0
     */
    @Nullable
    private String getExtensionFromFileHeader(@NotNull byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return null;
        }

        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            FileType fileType = ImageUtils.getFileType(is);
            if (fileType != null) {
                String ext = fileType.getExt();
                return ext != null && !ext.isEmpty() ? "." + ext : null;
            }
        } catch (IOException e) {
            log.trace("从文件头推断图片类型失败", e);
        }

        return null;
    }

    /**
     * 更新图片名称，添加或替换扩展名
     * <p>
     * 如果原图片名称已有扩展名，则替换为新的扩展名；如果没有扩展名，则添加新的扩展名。
     * 对于包含查询参数的 URL（如 `image.png?param=value`），会先移除查询参数，再处理扩展名。
     *
     * @param originalImageName 原始图片名称
     * @param extension         新的扩展名（带点号，如 ".jpg"）
     * @return 更新后的图片名称
     * @since 1.0.0
     */
    @NotNull
    private String updateImageNameWithExtension(@NotNull String originalImageName, @NotNull String extension) {
        // 移除查询参数（如 ?x-oss-process=...）
        String nameWithoutQuery = originalImageName.split("\\?")[0];

        // 检查是否已有扩展名
        int lastDot = nameWithoutQuery.lastIndexOf('.');
        int lastSlash = Math.max(nameWithoutQuery.lastIndexOf('/'), nameWithoutQuery.lastIndexOf('\\'));

        // 如果点号在最后一个斜杠之后，说明已有扩展名
        if (lastDot > lastSlash) {
            // 替换扩展名
            return nameWithoutQuery.substring(0, lastDot) + extension;
        } else {
            // 添加扩展名
            return nameWithoutQuery + extension;
        }
    }
}

