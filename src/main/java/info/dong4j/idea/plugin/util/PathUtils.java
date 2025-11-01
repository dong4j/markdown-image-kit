package info.dong4j.idea.plugin.util;

import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 路径处理工具类
 * <p>
 * 提供图片路径相关的工具方法，包括相对路径计算、绝对路径获取、路径前缀添加、URL 转义等功能。
 * 主要用于处理 Markdown 文档中图片路径的各种转换需求。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 2.1.0
 */
@SuppressWarnings("D")
public final class PathUtils {

    /**
     * 禁止实例化工具类
     * <p>
     * 该构造函数用于防止外部实例化 PathUtils 工具类，确保其作为静态工具类使用。
     *
     * @since 2.1.0
     */
    @Contract(" -> fail")
    private PathUtils() {
        throw new RuntimeException("Tool class does not support instantiation");
    }

    /**
     * 计算源文件相对于 Markdown 文档的相对路径
     * <p>
     * 该方法用于计算图片文件相对于当前 Markdown 文档的相对路径。
     * 例如：当前文档在 /project/docs/readme.md，图片在 /project/images/test.png
     * 则返回 ../images/test.png
     *
     * @param markdownFile Markdown 文档的虚拟文件对象
     * @param imageFile    图片文件的绝对路径
     * @return 图片相对于 Markdown 文档的相对路径
     * @since 2.1.0
     */
    @NotNull
    public static String calculateRelativePath(@NotNull VirtualFile markdownFile, @NotNull String imageFile) {
        File mdFile = new File(markdownFile.getPath());
        File imgFile = new File(imageFile);

        // 获取 Markdown 文件的父目录
        File mdParent = mdFile.getParentFile();
        if (mdParent == null) {
            return imgFile.getName();
        }

        // 使用 Path 类计算相对路径
        Path mdPath = mdParent.toPath();
        Path imgPath = imgFile.toPath();

        try {
            Path relativePath = mdPath.relativize(imgPath);
            // 统一使用正斜杠
            return normalizePathSeparator(relativePath.toString());
        } catch (IllegalArgumentException e) {
            // 如果无法计算相对路径（例如在不同的驱动器上），返回绝对路径
            return normalizePathSeparator(imageFile);
        }
    }

    /**
     * 计算目标目录相对于 Markdown 文档的相对路径
     * <p>
     * 该方法用于将图片保存到指定目录后，计算该目录下图片的相对路径。
     * 例如：当前文档在 /project/docs/readme.md，目标目录是 ./imgs
     * 图片名称是 test.png，则返回 imgs/test.png
     * 如果目标目录已经是绝对路径（如使用 ${project} 占位符后），则直接使用。
     *
     * @param markdownFile Markdown 文档的虚拟文件对象
     * @param targetDir    目标目录路径（可能是相对路径或绝对路径）
     * @param imageName    图片文件名
     * @return 图片相对于 Markdown 文档的相对路径
     * @since 2.1.0
     */
    @NotNull
    public static String calculateTargetRelativePath(@NotNull VirtualFile markdownFile,
                                                     @NotNull String targetDir,
                                                     @NotNull String imageName) {
        File mdFile = new File(markdownFile.getPath());
        File mdParent = mdFile.getParentFile();

        if (mdParent == null) {
            return imageName;
        }

        // 处理目标目录
        File targetDirectory;
        File targetDirFile = new File(targetDir);

        if (targetDirFile.isAbsolute()) {
            // 已经是绝对路径，直接使用（如使用 ${project} 占位符后）
            targetDirectory = targetDirFile;
        } else {
            // 相对路径，基于 Markdown 文件的父目录
            targetDirectory = new File(mdParent, targetDir);
        }

        File imageFile = new File(targetDirectory, imageName);

        // 计算相对路径
        Path mdPath = mdParent.toPath();
        Path imgPath = imageFile.toPath();

        try {
            Path relativePath = mdPath.relativize(imgPath);
            return normalizePathSeparator(relativePath.toString());
        } catch (IllegalArgumentException e) {
            // 无法计算相对路径时，返回图片名称
            return imageName;
        }
    }

    /**
     * 获取图片的绝对路径
     * <p>
     * 该方法根据目标目录和图片名称构建图片的绝对路径。
     * 如果目标目录是相对路径，则基于 Markdown 文件的位置计算。
     * 如果目标目录已经是绝对路径（如使用 ${project} 占位符后），则直接使用。
     * 返回的路径会被规范化，去除冗余的 "./" 和 "../" 等路径部分。
     *
     * @param markdownFile Markdown 文档的虚拟文件对象
     * @param targetDir    目标目录路径（可能是相对路径或绝对路径）
     * @param imageName    图片文件名
     * @return 图片的绝对路径（已规范化）
     * @since 2.1.0
     */
    @NotNull
    public static String getAbsolutePath(@NotNull VirtualFile markdownFile,
                                         @NotNull String targetDir,
                                         @NotNull String imageName) {
        File targetDirectory;

        // 检查 targetDir 是否已经是绝对路径
        File targetDirFile = new File(targetDir);
        if (targetDirFile.isAbsolute()) {
            // 已经是绝对路径，直接使用（如使用 ${project} 占位符后）
            targetDirectory = targetDirFile;
        } else {
            // 相对路径，基于 Markdown 文件的父目录
            File mdFile = new File(markdownFile.getPath());
            File mdParent = mdFile.getParentFile();

            if (mdParent == null) {
                // 如果无法获取父目录，直接使用 targetDir
                return normalizeAndCanonicalPath(new File(targetDir, imageName));
            }

            targetDirectory = new File(mdParent, targetDir);
        }

        File imageFile = new File(targetDirectory, imageName);
        return normalizeAndCanonicalPath(imageFile);
    }

    /**
     * 规范化并获取文件的规范路径
     * <p>
     * 该方法尝试获取文件的规范路径（canonical path），规范路径会：
     * 1. 解析所有的符号链接
     * 2. 去除冗余的路径分隔符（如 "./" 和 "../"）
     * 3. 统一路径格式
     * <p>
     * 如果无法获取规范路径（如文件不存在），则回退到绝对路径。
     *
     * @param file 文件对象
     * @return 规范化后的路径（使用正斜杠）
     * @since 2.1.0
     */
    @NotNull
    private static String normalizeAndCanonicalPath(@NotNull File file) {
        try {
            // 使用 getCanonicalPath() 可以解析 "./" 和 "../" 等路径
            String canonicalPath = file.getCanonicalPath();
            return normalizePathSeparator(canonicalPath);
        } catch (Exception e) {
            // 如果无法获取规范路径，回退到绝对路径
            return normalizePathSeparator(file.getAbsolutePath());
        }
    }

    /**
     * 为相对路径添加 "./" 前缀
     * <p>
     * 该方法用于在相对路径前添加 "./" 前缀，使路径更加明确。
     * 例如：imgs/test.png -> ./imgs/test.png
     * 如果路径已经以 "./" 或 "../" 开头，则不重复添加。
     *
     * @param path 原始路径
     * @return 添加了 "./" 前缀的路径（如果需要）
     * @since 2.1.0
     */
    @NotNull
    public static String addDotSlashPrefix(@NotNull String path) {
        if (path.startsWith("./") || path.startsWith("../") || path.startsWith("/")) {
            return path;
        }
        return "./" + path;
    }

    /**
     * 转义图片 URL 中的特殊字符
     * <p>
     * 该方法用于对图片路径中的特殊字符（如中文、空格等）进行 URL 编码。
     * 例如：./中文目录/img.png -> ./%E4%B8%AD%E6%96%87%E7%9B%AE%E5%BD%95/img.png
     * 注意：路径分隔符 "/" 不会被编码。
     *
     * @param path 原始路径
     * @return 转义后的路径
     * @since 2.1.0
     */
    @NotNull
    public static String escapeImageUrl(@NotNull String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }

        // 分割路径为各个部分，分别编码
        String[] parts = path.split("/", -1);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append("/");
            }

            String part = parts[i];
            // 对于空字符串、"."、".." 不进行编码
            if (part.isEmpty() || ".".equals(part) || "..".equals(part)) {
                result.append(part);
            } else {
                // 编码路径部分
                String encoded = URLEncoder.encode(part, StandardCharsets.UTF_8);
                // URL 编码会将空格转换为 +，需要替换为 %20
                encoded = encoded.replace("+", "%20");
                result.append(encoded);
            }
        }

        return result.toString();
    }

    /**
     * 规范化路径分隔符
     * <p>
     * 将路径中的反斜杠统一转换为正斜杠，确保跨平台兼容性。
     * Java 的 File 类可以接受正斜杠，即使在 Windows 上也能正确处理。
     *
     * @param path 原始路径
     * @return 规范化后的路径（统一使用正斜杠）
     * @since 2.1.0
     */
    @NotNull
    public static String normalizePathSeparator(@NotNull String path) {
        return path.replace('\\', '/');
    }

    /**
     * 判断路径是否为绝对路径
     * <p>
     * 该方法用于判断给定的路径是否为绝对路径。
     * 绝对路径的判断基于系统类型：
     * - Windows: 以驱动器字母开头（如 C:\）或 UNC 路径（如 \\server\share）
     * - Unix/Linux/macOS: 以 / 开头
     *
     * @param path 要判断的路径
     * @return 如果是绝对路径返回 true，否则返回 false
     * @since 2.1.0
     */
    public static boolean isAbsolutePath(@Nullable String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        return new File(path).isAbsolute();
    }
}

