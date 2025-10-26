# PicList 命令输出验证测试

## 测试目的

验证 PicList 命令行上传时是否会输出 URL 到 stdout/stderr，以解决并发上传时从剪贴板获取 URL 可能不准确的问题。

## 测试文件

- `src/test/java/info/dong4j/idea/plugin/client/PicListClientCommandTest.java`

## 运行测试

### 方式 1: 使用 Gradle 命令

```bash
# 运行单个测试类
./gradlew test --tests "info.dong4j.idea.plugin.client.PicListClientCommandTest"

# 运行单个测试方法
./gradlew test --tests "info.dong4j.idea.plugin.client.PicListClientCommandTest.testPicListCommandOutput"
```

### 方式 2: 配置 PicList 路径

在运行测试前，设置系统属性：

```bash
# macOS
-Dpiclist.exe.path=/Applications/PicList.app

# Windows
-Dpiclist.exe.path=C:\\Users\\YourName\\AppData\\Local\\PicList\\PicList.exe

# Linux
-Dpiclist.exe.path=/usr/local/bin/PicList.AppImage
```

### 方式 3: 使用 Maven（如果使用）

```bash
mvn test -Dtest=PicListClientCommandTest
```

## 测试内容

### testPicListCommandOutput()

**测试流程：**

1. **创建测试图片**：生成一个有效的 JPEG 文件（1x1 像素）
2. **执行 PicList 命令**：`PicList upload <file>`
3. **捕获输出**：
    - 读取 stdout
    - 读取 stderr
4. **验证结果**：
    - 检查退出码是否为 0
    - 检查输出中是否包含 URL（http:// 或 https://）
5. **输出日志**：记录 stdout 和 stderr 的完整内容

**预期结果：**

```
========================================
命令执行结果:
  退出码: 0
  STDOUT: https://example.com/uploaded.jpg
  STDERR: 
========================================
```

### testPicListVersion()

测试 PicList 版本信息输出，验证基本的命令执行。

## 可能的结果

### 结果 1: PicList 输出 URL 到 stdout

**日志输出：**

```
STDOUT 是否包含 URL: true
✓ PicList 会输出 URL 到标准输出或标准错误输出
```

**推荐方案：** 读取 stdout 获取 URL，不用剪贴板

### 结果 2: PicList 输出 URL 到 stderr

**日志输出：**

```
STDERR 是否包含 URL: true
✓ PicList 会输出 URL 到标准输出或标准错误输出
```

**推荐方案：** 读取 stderr 获取 URL

### 结果 3: PicList 不输出到 stdio

**日志输出：**

```
STDOUT 是否包含 URL: false
STDERR 是否包含 URL: false
✗ PicList 可能不会输出 URL 到 stdout/stderr，或者 URL 输出在剪贴板中
```

**推荐方案：** 继续使用剪贴板 + 重试机制，但需要其他验证方式

## 根据测试结果制定方案

### 如果 PicList 输出 URL 到 stdout/stderr

**实施方案：** 读取进程输出而不是剪贴板

```java
// 读取 stdout
String stdout = readProcessOutput(process.getInputStream());
String url = extractUrlFromOutput(stdout);
```

**优点：**

- ✅ 并发安全（每个进程独立输出）
- ✅ 准确（直接从命令获取）
- ✅ 无需等待剪贴板

### 如果 PicList 只输出到剪贴板

**需要改进方案：**

#### 选项 A: 基于文件标识符

```java
// 1. 生成唯一的文件名
String uniqueFileName = "upload_" + System.currentTimeMillis() + "_" + 
                       fileHash + ".png";

// 2. 重命名文件
tempFile.renameTo(new File(tempFile.getParent(), uniqueFileName));

// 3. 执行上传
process.execute();

// 4. 从剪贴板获取 URL 时，通过某种方式验证是否对应我们的文件
//    - 通过文件名验证
//    - 通过上传时间验证
//    - 通过文件大小验证
```

#### 选项 B: 使用日志文件

```java
// 1. PicList 可能支持输出到日志文件
String logFile = System.getProperty("user.home") + "/.piclist-upload.log";

// 2. 执行命令并监控日志文件
process.execute();

// 3. 读取日志文件获取 URL
String url = readUrlFromLogFile(logFile);
```

#### 选项 C: 使用同步锁

```java
// 单线程执行命令行上传
private static final Object uploadLock = new Object();

public String uploadViaCommandLine() {
    synchronized(uploadLock) {
        process.execute();
        return getUrlFromClipboardWithRetry();
    }
}
```

## 并发问题的根本原因

```
线程 A: 开始上传 image1.jpg
线程 B: 开始上传 image2.jpg  
    ↓
线程 A: 执行 PicList upload image1.jpg
线程 B: 执行 PicList upload image2.jpg
    ↓
线程 A: 从剪贴板获取 URL → 得到 image2.jpg 的 URL ❌
线程 B: 从剪贴板获取 URL → 得到 image2.jpg 的 URL ✓
```

**根本原因：** 剪贴板是全局共享的，多线程并发时会被覆盖。

## 运行测试示例

### 完整命令

```bash
# macOS
./gradlew test \
  --tests "info.dong4j.idea.plugin.client.PicListClientCommandTest" \
  -Dpiclist.exe.path=/Applications/PicList.app

# Windows
./gradlew test --tests "info.dong4j.idea.plugin.client.PicListClientCommandTest" -Dpiclist.exe.path=C:\Users\YourName\AppData\Local\PicList\PicList.exe
```

### 查看测试日志

测试会输出详细的日志信息，包括：

- 命令执行结果
- stdout 内容
- stderr 内容
- 是否包含 URL

## 下一步行动

根据测试结果决定实现方案：

1. **如果 stdout/stderr 包含 URL**：
    - 修改 `uploadViaCommandLine()` 读取进程输出
    - 移除剪贴板读取逻辑
    - 提供更可靠的并发上传支持

2. **如果只输出到剪贴板**：
    - 保持现有重试机制
    - 添加更严格的验证逻辑（文件标识符）
    - 考虑使用同步锁或排队机制
