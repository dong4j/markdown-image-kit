# WebP 图片上传问题修复

## 问题描述

当开启 webp 转换功能后，如果上传已经是 webp 格式的图片到自定义图床，会出现上传失败的问题，错误提示 `inputStream` 为空。

## 根本原因

在 `ImageCompressionHandler` 处理流程中：

1. **所有图片都会被读取**：为了支持重新读取和格式转换，原始 `inputStream` 会被完全读取到字节数组中
2. **原始流会被关闭**：读取完成后，原始 `inputStream` 会被关闭以释放资源
3. **webp 格式图片的特殊处理**：当检测到图片已经是 webp 格式且只开启了 webp 转换时，代码会跳过转换处理
4. **Bug**：在跳过转换的情况下，代码没有重新设置 `inputStream`，导致后续上传时无法读取图片数据

### 问题代码

```java
// 情况3：如果只开启了转换成webp，尝试转webp，失败就不压缩
else if (isWebpEnabled) {
    if (!alreadyWebp) {
        boolean webpSuccess = tryConvertToWebp(markdownImage, originalBytes, webpQuality, imageName);
        if (!webpSuccess) {
            markdownImage.setInputStream(new ByteArrayInputStream(originalBytes));
        }
    }
    // 如果已经是webp，不需要处理
    // ⚠️ Bug: 没有重新设置 inputStream，但原始流已被关闭
}
```

## 修复方案

在检测到图片已经是 webp 格式时，重新创建一个 `ByteArrayInputStream` 并设置给 `markdownImage`：

```java
else if (isWebpEnabled) {
    if (!alreadyWebp) {
        boolean webpSuccess = tryConvertToWebp(markdownImage, originalBytes, webpQuality, imageName);
        if (!webpSuccess) {
            markdownImage.setInputStream(new ByteArrayInputStream(originalBytes));
        }
    } else {
        // 如果已经是webp，不需要转换，但需要重新设置输入流（因为原始流已被读取）
        log.trace("图片已经是webp格式，不需要转换: {}", imageName);
        markdownImage.setInputStream(new ByteArrayInputStream(originalBytes));
    }
}
```

## 影响范围

### 修复前

以下场景会受到影响：

1. 右键选择已有的 webp 格式图片，上传到图床
2. 先粘贴 PNG 图片并转换为 webp 保存，然后右键上传该 webp 文件
3. 任何需要上传已存在的 webp 格式图片的场景

### 修复后

所有 webp 格式图片都能正常上传，无论是：

- 直接上传 webp 文件
- PNG/JPG 转换为 webp 后上传
- 已经保存的 webp 文件再次上传

## 测试建议

### 测试场景 1：上传已有的 webp 图片

1. 准备一个 webp 格式的图片文件
2. 在 IDEA 中右键选择该文件
3. 选择"上传到图床"
4. 开启 webp 转换选项
5. 验证图片能够成功上传

### 测试场景 2：转换后再上传

1. 开启 webp 转换选项
2. 在 Markdown 文件中粘贴一张 PNG 图片
3. 图片会被转换为 webp 并保存到本地
4. 右键选择该 webp 文件
5. 选择"上传到图床"
6. 验证图片能够成功上传

### 测试场景 3：直接粘贴并上传

1. 开启 webp 转换选项
2. 设置粘贴时自动上传到图床
3. 在 Markdown 文件中粘贴一张 PNG 图片
4. 验证图片被转换为 webp 并成功上传

## 相关代码

- 修复文件：`ImageCompressionHandler.java`
- 相关处理链：
    - `ActionManager.buildUploadChain()` - 上传处理链
    - `PasteImageAction` - 粘贴处理链
    - `ImageUploadAction.buildChain()` - 右键上传处理链

## 技术细节

### 为什么需要重新设置 inputStream？

Java 的 `InputStream` 是一次性的，读取后位置会移到末尾。虽然 `ByteArrayInputStream` 支持 `reset()`，但：

1. `transferTo()` 方法会完全读取流
2. 后续代码会关闭原始流
3. 即使不关闭，流的位置也在末尾，无法再次读取

因此，最佳实践是从字节数组重新创建一个新的 `ByteArrayInputStream`。

### 为什么其他格式没问题？

其他情况都会调用 `tryConvertToWebp()` 或 `compressImage()`，这两个方法都会创建新的 `ByteArrayInputStream`：

```java
// tryConvertToWebp
markdownImage.setInputStream(new ByteArrayInputStream(webpBytes));

// compressImage
markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
```

只有"图片已经是 webp"的情况被遗漏了。

## 修复日期

2025-11-02

## 修复人

AI Assistant (Claude Sonnet 4.5)

