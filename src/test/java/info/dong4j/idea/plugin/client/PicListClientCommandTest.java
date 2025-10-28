package info.dong4j.idea.plugin.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PicList 命令行上传测试类
 * <p>
 * 该类用于验证 PicList 命令行工具在执行上传操作时的行为，包括输出内容的验证和版本信息的检查。
 * 主要测试 PicList 是否会在标准输出或标准错误中输出图片上传的 URL。
 *
 * @author dong4j
 * @date 2025.10.27
 */
@SuppressWarnings("D")
@Slf4j
public class PicListClientCommandTest {
    /** PicList 可执行文件路径（需要根据实际环境修改） */
    private static final String PICLIST_EXE_PATH = System.getProperty("piclist.exe.path",
                                                                      "/Applications/PicList.app/Contents/MacOS/PicList");

    /**
     * 测试前执行的初始化方法，用于设置测试路径信息
     * <p>
     * 在每次测试方法执行前调用，记录测试路径到日志中
     *
     * @since 1.0
     */
    @BeforeEach
    public void setUp() {
        log.info("PicList 测试路径: {}", PICLIST_EXE_PATH);
    }

    /**
     * 测试 PicList 命令行执行的输出
     * <p>
     * 测试场景：验证 PicList 上传图片时是否会输出 URL 到 stdout 或 stderr
     * 预期结果：命令执行成功后，stdout 或 stderr 应包含有效的 URL（http:// 或 https://）
     * <p>
     * 注意：测试依赖 PicList 可执行文件存在，若不存在则跳过测试。测试过程中会执行 PicList 命令并读取其输出。
     * <p>
     * 验证逻辑：检查 stdout 和 stderr 是否包含 URL 格式字符串，若存在则输出成功提示，否则记录警告。
     */
    @Test
    public void testPicListCommandOutput(@TempDir Path tempDir) throws Exception {
        // 检查 PicList 是否存在
        File picListExe = new File(resolveExecutablePath(PICLIST_EXE_PATH));
        if (!picListExe.exists()) {
            log.warn("PicList 可执行文件不存在: {}, 跳过测试", picListExe.getAbsolutePath());
            return;
        }

        log.info("PicList 可执行文件路径: {}", picListExe.getAbsolutePath());

        // 创建测试图片文件
        // File testImageFile = createTestImageFile(tempDir.toFile(), "test.jpg");
        File testImageFile = new File("/Users/dong4j/Downloads/IntelliJ IDEA 2025-10-26 16.07.02.png");

        // 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder(
            picListExe.getAbsolutePath(),
            "upload",
            testImageFile.getAbsolutePath()
        );

        processBuilder.redirectErrorStream(false); // stderr 单独处理

        log.info("执行命令: {} upload {}", picListExe.getName(), testImageFile.getAbsolutePath());
        Process process = processBuilder.start();

        // 读取 stdout
        StringBuilder stdout = new StringBuilder();
        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                    log.debug("STDOUT: {}", line);
                }
            } catch (IOException e) {
                log.error("读取 stdout 失败", e);
            }
        });
        stdoutThread.start();

        // 读取 stderr
        StringBuilder stderr = new StringBuilder();
        Thread stderrThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                    log.debug("STDERR: {}", line);
                }
            } catch (IOException e) {
                log.error("读取 stderr 失败", e);
            }
        });
        stderrThread.start();

        // 等待命令执行完成
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);

        // 等待输出线程完成
        stdoutThread.join(1000);
        stderrThread.join(1000);

        // 获取输出内容
        String stdoutContent = stdout.toString();
        String stderrContent = stderr.toString();
        int exitCode = finished ? process.exitValue() : -1;

        log.info("========================================");
        log.info("命令执行结果:");
        log.info("  退出码: {}", exitCode);
        log.info("  STDOUT: {}", stdoutContent);
        log.info("  STDERR: {}", stderrContent);
        log.info("========================================");

        // 验证结果
        assertTrue(finished, "命令执行超时");
        assertEquals(0, exitCode, "命令执行失败，退出码: " + exitCode);

        // 检查输出
        boolean hasUrlInStdout = stdoutContent.contains("http://") || stdoutContent.contains("https://");
        boolean hasUrlInStderr = stderrContent.contains("http://") || stderrContent.contains("https://");

        log.info("STDOUT 是否包含 URL: {}", hasUrlInStdout);
        log.info("STDERR 是否包含 URL: {}", hasUrlInStderr);

        // 断言或日志输出
        if (hasUrlInStdout || hasUrlInStderr) {
            log.info("✓ PicList 会输出 URL 到标准输出或标准错误输出");
        } else {
            log.warn("✗ PicList 可能不会输出 URL 到 stdout/stderr，" +
                     "或者 URL 输出在剪贴板中");
        }
    }

    /**
     * 测试 PicList 命令行版本信息获取功能
     * <p>
     * 测试场景：检查 PicList 可执行文件是否存在，并尝试通过命令行参数 "--version" 获取其版本信息
     * 预期结果：若 PicList 存在，应成功执行并输出版本信息及退出码；若不存在，应记录警告并跳过测试
     * <p>
     * 注意：该测试需要系统中存在 PicList 可执行文件，且路径配置正确。测试过程中会启动外部进程，可能需要适当调整超时时间或权限
     */
    @Test
    public void testPicListVersion() throws Exception {
        // 检查 PicList 是否存在
        File picListExe = new File(resolveExecutablePath(PICLIST_EXE_PATH));
        if (!picListExe.exists()) {
            log.warn("PicList 可执行文件不存在: {}, 跳过测试", picListExe.getAbsolutePath());
            return;
        }

        // 尝试获取版本信息（如果支持）
        String[] command = {picListExe.getAbsolutePath(), "--version"};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        int exitCode = finished ? process.exitValue() : -1;

        log.info("PicList 版本信息:");
        log.info("  退出码: {}", exitCode);
        log.info("  输出: {}", output);
    }

    /**
     * 在指定目录下创建一个测试图片文件
     * <p>
     * 该方法会创建一个最小有效的 JPEG 格式图片文件（1x1 像素的红色图片），用于测试目的。
     *
     * @param directory 目标目录，用于存储生成的图片文件
     * @param filename  生成的图片文件名
     * @return 创建成功的图片文件对象
     * @throws IOException 如果文件创建过程中发生异常
     */
    private File createTestImageFile(File directory, String filename) throws IOException {
        File file = new File(directory, filename);

        // 创建一个最小有效的 JPEG 文件（1x1 像素的红色图片）
        // JPEG 文件头
        byte[] jpegBytes = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10,
            0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48,
            0x00, 0x48, 0x00, 0x00, (byte) 0xFF, (byte) 0xDB, 0x00, 0x43,
            0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07,
            0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B,
            0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F,
            0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20,
            0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30,
            0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32,
            0x3C, 0x2E, 0x33, 0x34, 0x32, (byte) 0xFF, (byte) 0xC0, 0x00,
            0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
            (byte) 0xFF, (byte) 0xC4, 0x00, 0x1F, 0x00, 0x00, 0x01, 0x05,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x07, 0x08, 0x09, 0x0A, 0x0B, (byte) 0xFF, (byte) 0xC4, (byte) 0x00,
            (byte) 0xB5, 0x10, 0x00, 0x02, 0x01, 0x03, 0x03, 0x02, 0x04, 0x03,
            0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01, (byte) 0x7D, 0x01, 0x02,
            0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13,
            0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, (byte) 0x81, (byte) 0x91,
            (byte) 0xA1, 0x08, 0x23, 0x42, (byte) 0xB1, (byte) 0xC1, 0x15, 0x52,
            (byte) 0xD1, (byte) 0xF0, 0x24, 0x33, 0x62, 0x72, (byte) 0x82, 0x09,
            0x0A, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x25, 0x26, 0x27, 0x28, 0x29,
            0x2A, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x43, 0x44, 0x45,
            0x46, 0x47, 0x48, 0x49, 0x4A, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
            0x59, 0x5A, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x73,
            0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, (byte) 0x83, (byte) 0x84,
            (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8A,
            (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97,
            (byte) 0x98, (byte) 0x99, (byte) 0x9A, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4,
            (byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8, (byte) 0xA9, (byte) 0xAA,
            (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7,
            (byte) 0xB8, (byte) 0xB9, (byte) 0xBA, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4,
            (byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0xCA,
            (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
            (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3,
            (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7, (byte) 0xE8, (byte) 0xE9,
            (byte) 0xEA, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5,
            (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0xFA, (byte) 0xFF,
            (byte) 0xDA, 0x00, 0x0C, 0x03, 0x01, 0x00, 0x02, 0x11, 0x03, 0x11,
            0x00, 0x3F, 0x00, (byte) 0x80, (byte) 0xFF, (byte) 0xD9
        };

        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
        fos.write(jpegBytes);
        fos.close();

        log.debug("创建测试图片文件: {}", file.getAbsolutePath());
        return file;
    }

    /**
     * 解析可执行文件路径，处理 macOS 系统下的 .app 目录结构
     * <p>
     * 根据操作系统类型判断是否为 macOS，并对 .app 目录进行特殊处理，返回实际可执行文件路径。
     *
     * @param exePath 可执行文件路径
     * @return 处理后的可执行文件路径
     */
    private String resolveExecutablePath(String exePath) {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            File file = new File(exePath);

            if (file.isDirectory() && exePath.endsWith(".app")) {
                File executableFile = new File(file, "Contents/MacOS/PicList");
                if (executableFile.exists() && executableFile.canExecute()) {
                    return executableFile.getAbsolutePath();
                }

                File macOsDir = new File(file, "Contents/MacOS");
                if (macOsDir.exists() && macOsDir.isDirectory()) {
                    File[] files = macOsDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile() && f.canExecute()) {
                                return f.getAbsolutePath();
                            }
                        }
                    }
                }
            }
        }

        return exePath;
    }
}
