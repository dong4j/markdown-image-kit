# PicList 命令行上传支持

## 功能概述

PicList 图床客户端现在支持两种上传方式：

1. **API 上传**：通过 HTTP API 接口上传（默认方式）
2. **命令行上传**：通过 PicList 命令行工具上传（配置了可执行文件路径时）

## 实现原理

### 自动选择上传方式

```java
@Override
public String upload(InputStream inputStream, String fileName) throws Exception {
    // 如果配置了可执行文件路径，使用命令行方式上传
    if (StringUtils.isNotEmpty(exePath)) {
        return uploadViaCommandLine(inputStream, fileName);
    }
    
    // 否则使用 API 方式上传
    return uploadViaApi(inputStream, fileName);
}
```

### 命令行上传流程

1. **保存临时文件**：将输入流保存到临时文件
2. **执行命令**：调用 PicList 命令行工具上传文件
3. **获取结果**：从系统剪贴板获取上传后的 URL
4. **清理临时文件**：删除临时文件

### 命令行执行

```java
// Windows
"<PicList安装路径>\PicList.exe" upload "文件路径"

// macOS
"/Applications/PicList.app/Contents/MacOS/PicList" upload "文件路径"

// Linux
"<PicList安装路径>/PicList.AppImage" upload "文件路径"
```

### 从剪贴板获取结果

PicList 命令行工具上传成功后会将 URL 复制到系统剪贴板：

```java
private String getUrlFromClipboard() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
        if (clipboardData instanceof String) {
            String text = (String) clipboardData;
            // 检查是否是 URL
            if (text.startsWith("http://") || text.startsWith("https://")) {
                return text.trim();
            }
        }
    }
    return "";
}
```

## 配置说明

### 新增配置字段

在 `PicListOssState` 中添加了 `exePath` 字段：

```java
/** PicList 命令行可执行文件路径 */
private String exePath = "";
```

### UI 配置要求

需要在设置界面中添加以下 UI 组件：

- **picListExeTextField**: 命令行路径输入框
- **picListExeBrowseButton**: 浏览按钮（可选）

### 提示信息

```java
public final static String EXE_PATH_HINT = "PicList 可执行文件路径（留空则使用 API 上传）";
```

## 使用方式

### 1. API 上传（默认）

如果 `exePath` 为空，使用 API 上传：

- 接口地址：`http://127.0.0.1:36677/upload`
- 使用 multipart/form-data 格式
- 支持查询参数（picbed、configName、key）

### 2. 命令行上传

如果 `exePath` 不为空，使用命令行上传：

- 要求安装 PicList 并配置可执行文件路径
- 上传时将文件保存到临时文件
- 执行命令行上传
- 从剪贴板获取结果 URL

### 配置示例

**Windows**:

```
exePath: C:\Users\YourName\AppData\Local\PicList\PicList.exe
```

**macOS**:

```
exePath: /Applications/PicList.app/Contents/MacOS/PicList
```

**Linux**:

```
exePath: /usr/local/bin/PicList.AppImage
```

## 优势

1. **灵活性**：支持两种上传方式，满足不同场景需求
2. **自动切换**：根据配置自动选择最合适的上传方式
3. **向后兼容**：默认使用 API 上传，不影响现有用户
4. **性能优化**：命令行上传可直接利用 PicList 的配置和功能

## 技术细节

### 临时文件管理

- 使用 `File.createTempFile()` 创建临时文件
- 前缀：`piclist-upload-`
- 上传完成后自动删除
- 支持异常情况下的清理

### 命令执行

- 使用 `ProcessBuilder` 执行命令行
- 设置 10 秒超时
- 捕获进程退出码
- 重定向错误输出

### 平台兼容

- 自动检测操作系统
- Windows/macOS/Linux 统一处理
- 命令格式根据平台调整

## 错误处理

### 超时处理

```java
boolean finished = process.waitFor(10, TimeUnit.SECONDS);
if (!finished) {
    process.destroyForcibly();
    throw new RuntimeException("命令行执行超时");
}
```

### 退出码检查

```java
int exitCode = process.exitValue();
if (exitCode != 0) {
    throw new RuntimeException("命令行执行失败，退出码: " + exitCode);
}
```

### 剪贴板获取失败

```java
String url = getUrlFromClipboard();
if (StringUtils.isBlank(url)) {
    throw new RuntimeException("未能从剪贴板获取上传结果");
}
```

## 注意事项

1. **路径准确性**：确保可执行文件路径正确
2. **权限问题**：确保 PicList 可执行文件有执行权限
3. **剪贴板权限**：确保有访问剪贴板的权限
4. **网络连接**：命令行上传仍需网络连接（如果上传到远程图床）
5. **临时文件**：临时文件会自动清理，但磁盘空间充足时更稳定

## 测试建议

### 测试 API 上传

1. 不配置 `exePath`
2. 确保 PicList 服务运行在指定端口
3. 点击 Test 按钮上传测试图片

### 测试命令行上传

1. 配置 `exePath` 为正确的 PicList 可执行文件路径
2. 确保 PicList 已正确安装
3. 点击 Test 按钮上传测试图片
4. 检查剪贴板中是否包含上传后的 URL

## 参考资料

- [PicList 官方文档](https://piclist.cn/advanced.html)
- [PicList 命令行使用](https://piclist.cn/advanced.html#命令行上传)
