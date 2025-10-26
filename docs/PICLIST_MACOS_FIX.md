# PicList macOS .app 目录处理修复

## 问题描述

在 macOS 上选择 PicList.app 时，出现权限错误：

```
Cannot run program "/Applications/PicList.app": error=13, Permission denied
```

## 原因分析

macOS 的 `.app` 文件实际上是一个**目录包（Bundle）**，真正的可执行文件位于：

```
PicList.app/Contents/MacOS/PicList
```

直接执行 `.app` 目录会失败，因为：

1. `.app` 是一个目录，不是可执行文件
2. 需要执行 `.app/Contents/MacOS/` 目录下的实际可执行文件

## 解决方案

添加了 `resolveExecutablePath()` 方法来智能处理 macOS 的 `.app` 目录结构。

### 代码实现

```java
private String resolveExecutablePath(String exePath) {
    String osName = System.getProperty("os.name").toLowerCase();
    
    // macOS 需要特殊处理 .app 目录
    if (osName.contains("mac")) {
        File file = new File(exePath);
        
        // 如果是 .app 目录，需要找到内部的可执行文件
        if (file.isDirectory() && exePath.endsWith(".app")) {
            // 首先尝试查找 PicList 可执行文件
            File executableFile = new File(file, "Contents/MacOS/PicList");
            if (executableFile.exists() && executableFile.canExecute()) {
                log.debug("macOS 检测到 .app 目录，使用可执行文件: {}", executableFile.getAbsolutePath());
                return executableFile.getAbsolutePath();
            }
            
            // 如果 PicList 不存在，查找 MacOS 目录中的第一个可执行文件
            File macOsDir = new File(file, "Contents/MacOS");
            if (macOsDir.exists() && macOsDir.isDirectory()) {
                File[] files = macOsDir.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        if (f.isFile() && f.canExecute()) {
                            log.debug("macOS 在 MacOS 目录中找到可执行文件: {}", f.getAbsolutePath());
                            return f.getAbsolutePath();
                        }
                    }
                }
            }
            
            // 无法找到 .app 内的可执行文件
            throw new RuntimeException("无法在 .app 目录中找到可执行文件: " + exePath + 
                                      "\n请确保选择了正确的 PicList/PicGo .app 包");
        }
    }
    
    // 验证文件是否存在且可执行（Windows 和 Linux）
    File file = new File(exePath);
    if (!file.exists()) {
        throw new RuntimeException("可执行文件不存在: " + exePath);
    }
    if (!file.canExecute()) {
        throw new RuntimeException("文件没有执行权限: " + exePath + 
                                  "\n请使用 chmod +x 命令添加执行权限");
    }
    
    return exePath;
}
```

## 处理逻辑

### macOS .app 处理流程

```
1. 检测是否为 .app 目录
   ↓
2. 尝试查找 Contents/MacOS/PicList
   ↓ 找不到
3. 查找 Contents/MacOS/ 目录中的第一个可执行文件
   ↓ 找不到
4. 抛出友好的错误提示
```

### Windows/Linux 处理流程

```
1. 直接验证文件是否存在
   ↓
2. 验证文件是否有执行权限
   ↓
3. 返回可执行文件路径
```

## 使用示例

### macOS 配置

用户可以选择 `.app` 目录：

```
/Applications/PicList.app
```

插件会自动解析为：

```
/Applications/PicList.app/Contents/MacOS/PicList
```

### Windows 配置

用户直接选择可执行文件：

```
C:\Users\YourName\AppData\Local\PicList\PicList.exe
```

### Linux 配置

用户选择 AppImage：

```
/usr/local/bin/PicList.AppImage
```

## 错误提示

### 场景 1：找不到可执行文件

```
无法在 .app 目录中找到可执行文件: /Applications/PicList.app
请确保选择了正确的 PicList/PicGo .app 包
```

### 场景 2：文件不存在

```
可执行文件不存在: /path/to/non-existent/file
```

### 场景 3：无执行权限

```
文件没有执行权限: /path/to/file
请使用 chmod +x 命令添加执行权限
```

## 测试建议

1. 在 macOS 上选择 PicList.app
2. 验证插件自动找到 Contents/MacOS/PicList
3. 检查日志输出，确认使用的可执行文件路径
4. 测试上传功能是否正常工作

## 额外支持

### 支持 PicGo

该实现也兼容 PicGo，因为两者使用相同的 `.app` 结构。

### 自动查找机制

如果找不到 `PicList` 可执行文件，会自动查找 `MacOS` 目录中的第一个可执行文件，这样可以支持：

- PicList
- PicGo
- 其他使用相同结构的应用

## 参考资料

- [macOS Bundle 结构文档](https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html)
- [PicList GitHub](https://github.com/Kuingsmile/PicList)
- [PicGo GitHub](https://github.com/Molunerfinn/PicGo)
