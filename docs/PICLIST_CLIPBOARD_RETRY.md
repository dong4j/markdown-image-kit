# PicList 剪贴板获取 URL 重试机制

## 问题描述

在使用 PicList 命令行上传时，PicList 需要时间完成上传并将 URL 复制到剪贴板。如果立即从剪贴板获取，可能会获取不到 URL，导致上传失败。

## 解决方案

添加了 `getUrlFromClipboardWithRetry()` 方法，实现重试机制。

### 重试策略

```java
private String getUrlFromClipboardWithRetry() {
    int maxRetries = 10;      // 最多尝试 10 次
    long delayMs = 500;       // 每次间隔 500ms
    // 总等待时间约 5 秒
}
```

### 流程说明

```
命令执行成功
    ↓
第 1 次尝试获取剪贴板
    ↓ 未获取到
等待 500ms
    ↓
第 2 次尝试获取剪贴板
    ↓ 未获取到
等待 500ms
    ↓
... (最多 10 次)
    ↓
成功获取 URL 或 返回空
```

### 日志输出

```
[DEBUG] 第 1 次尝试，剪贴板中尚未有 URL，等待 500ms 后重试...
[DEBUG] 第 2 次尝试，剪贴板中尚未有 URL，等待 500ms 后重试...
[DEBUG] 第 3 次尝试，成功获取 URL: https://example.com/image.jpg
[INFO] 上传成功: mik.webp -> https://example.com/image.jpg
```

### 错误处理

如果 10 次尝试后仍未获取到 URL：

```
未能从剪贴板获取上传结果。
提示：请确保 PicList 已正确上传图片并将 URL 复制到剪贴板。
```

## 实现细节

### 1. 重试逻辑

```java
private String getUrlFromClipboardWithRetry() {
    int maxRetries = 10;
    long delayMs = 500;
    
    for (int i = 0; i < maxRetries; i++) {
        String url = getUrlFromClipboard();
        if (StringUtils.isNotBlank(url)) {
            log.debug("第 {} 次尝试，成功获取 URL: {}", i + 1, url);
            return url;
        }
        
        // 等待后重试
        if (i < maxRetries - 1) {
            Thread.sleep(delayMs);
        }
    }
    
    log.warn("尝试 {} 次后仍未从剪贴板获取到 URL", maxRetries);
    return "";
}
```

### 2. 中断处理

```java
try {
    Thread.sleep(delayMs);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    log.error("等待被中断", e);
    return "";
}
```

### 3. URL 验证

```java
private String getUrlFromClipboard() {
    // 检查是否是 URL
    if (text.startsWith("http://") || text.startsWith("https://")) {
        return text.trim();
    }
}
```

## 配置建议

### 调整重试参数

如果需要更长的等待时间，可以修改以下参数：

```java
int maxRetries = 20;      // 增加尝试次数到 20 次
long delayMs = 1000;     // 增加等待间隔到 1 秒
// 总等待时间约 20 秒
```

### 按文件大小调整

```java
// 根据文件大小动态调整等待时间
long estimatedWaitMs = calculateUploadTime(fileName);
long delayMs = Math.max(500, estimatedWaitMs / maxRetries);
```

## 监控和调试

### 日志级别

使用 `log.debug()` 记录每次尝试，便于调试：

```java
log.debug("第 {} 次尝试，剪贴板中尚未有 URL，等待 {}ms 后重试...", i + 1, delayMs);
log.debug("第 {} 次尝试，成功获取 URL: {}", i + 1, url);
log.warn("尝试 {} 次后仍未从剪贴板获取到 URL", maxRetries);
```

### 常见问题

#### 问题 1: 总是需要多次重试

**原因：** 网络较慢或文件较大

**解决方案：**

- 增加 maxRetries
- 增加 delayMs
- 检查网络连接

#### 问题 2: 超时后仍未获取到 URL

**原因：**

- PicList 上传失败
- PicList 配置错误
- 剪贴板被其他程序修改

**解决方案：**

1. 检查 PicList 日志
2. 手动测试 PicList 上传
3. 验证剪贴板内容

## 测试建议

### 测试场景 1: 正常上传

1. 配置 PicList 可执行文件路径
2. 上传测试图片
3. 观察日志，检查重试次数
4. 验证获取的 URL

### 测试场景 2: 慢速上传

1. 使用较大的图片文件（>1MB）
2. 上传并观察重试机制
3. 验证是否能成功获取 URL

### 测试场景 3: 失败场景

1. 配置错误的 PicList 路径
2. 上传测试图片
3. 验证错误提示信息

## 性能影响

### 时间成本

- **最快情况**：第一次尝试成功，耗时 < 100ms
- **一般情况**：2-3 次重试成功，耗时 1-1.5s
- **最慢情况**：10 次重试失败，耗时 5s

### 优化建议

1. **自适应等待**：根据图片大小调整延迟
2. **并行检查**：在等待时可以做其他工作
3. **超时保护**：设置总超时时间

## 参考资料

- [PicList 命令行使用](https://piclist.cn/advanced.html#命令行上传)
- [Java Clipboard API](https://docs.oracle.com/javase/8/docs/api/java/awt/datatransfer/Clipboard.html)
