# PicList 图床功能实现总结

## 实现概述

已成功在 Markdown Image Kit 插件中集成 PicList 图床功能，支持两种上传方式：**API 上传**和**命令行上传**。

## 已完成的文件

### 核心功能文件

1. **CloudEnum.java** - 添加 PICLIST 枚举项
2. **PicListOssState.java** - 状态管理类
3. **PicListClient.java** - 客户端实现（支持 API 和命令行上传）
4. **PicListOssSetting.java** - 设置类
5. **MikState.java** - 注册 PicListState
6. **OssState.java** - 添加 PicList 状态查询
7. **ProjectSettingsPage.java** - 集成 PicList 到设置页面

### 文档文件

1. **PICLIST_UI_SETUP.md** - UI 设置指南
2. **PICLIST_COMMAND_LINE_SUPPORT.md** - 命令行上传支持说明
3. **文件选择组件实现示例.md** - TextFieldWithBrowseButton 实现指南

## 核心功能特性

### 1. 自动选择上传方式

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

### 2. TextFieldWithBrowseButton 支持

使用 IntelliJ 平台的 `TextFieldWithBrowseButton` 组件实现了文件选择功能：

```java
private TextFieldWithBrowseButton picListExeTextField;

// 初始化浏览按钮
private void initPicListExeBrowser() {
    FileChooserDescriptor descriptor = new FileChooserDescriptor(
        true, false, false, false
    );
    descriptor.setTitle("选择 PicList 可执行文件");
    descriptor.setDescription("选择 PicList 命令行工具的可执行文件");
    
    descriptor.withFileFilter(virtualFile -> {
        String name = virtualFile.getName().toLowerCase();
        return name.endsWith(".exe") || 
               name.endsWith(".app") || 
               name.endsWith(".appimage");
    });
    
    picListExeTextField.addBrowseFolderListener(
        descriptor.getTitle(),
        descriptor.getDescription(),
        null,
        descriptor,
        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
    );
}
```

### 3. 从剪贴板获取结果

PicList 命令行上传成功后会将 URL 复制到剪贴板：

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

## UI 配置需要完成的工作

### 在 GUI Designer 中

1. 打开 `ProjectSettingsPage.form`
2. 在 `authorizationTabbedPanel` 中添加 PicList Tab（index 8）
3. 在 PicList Tab 中创建 `picListAuthorizationPanel`
4. 添加以下 UI 组件：

| 组件名                          | 类型                            | 说明        |
|------------------------------|-------------------------------|-----------|
| `picListApiTextField`        | JTextField                    | API 接口地址  |
| `picListPicbedTextField`     | JTextField                    | 图床类型（可选）  |
| `picListConfigNameTextField` | JTextField                    | 配置名称（可选）  |
| `picListKeyTextField`        | JPasswordField                | 密钥（可选）    |
| `picListExeTextField`        | **TextFieldWithBrowseButton** | 命令行路径（可选） |

### TextFieldWithBrowseButton 配置

在 GUI Designer 中：

1. 从组件面板拖入 `TextFieldWithBrowseButton`
2. 设置 binding 为 `picListExeTextField`
3. 设置 toolTipText 提示信息
4. 调整大小和布局

## 配置字段说明

### PicListOssState 配置项

| 字段           | 类型     | 必填 | 默认值                             | 说明       |
|--------------|--------|----|---------------------------------|----------|
| `api`        | String | ✅  | `http://127.0.0.1:36677/upload` | API 接口地址 |
| `picbed`     | String | ❌  | 空                               | 图床类型     |
| `configName` | String | ❌  | 空                               | 配置名称     |
| `key`        | String | ❌  | 空                               | 密钥       |
| `exePath`    | String | ❌  | 空                               | 可执行文件路径  |

### 上传方式选择

- **exePath 为空** → 使用 API 上传
- **exePath 不为空** → 使用命令行上传

## 使用方法

### 配置 PicList

1. 打开插件设置（Settings → Markdown Image Kit）
2. 选择 PicList Tab
3. 配置以下参数：
    - **API 接口**：`http://127.0.0.1:36677/upload`
    - **图床类型**（可选）：如 `qiniu`、`aws-s3` 等
    - **配置名称**（可选）：如 `piclist-test`
    - **密钥**（可选）：用于接口鉴权
    - **命令行路径**（可选）：PicList 可执行文件路径
4. 点击 **Test** 按钮测试
5. 选择 PicList 作为默认图床（可选）

### 命令行路径示例

**Windows**:

```
C:\Users\YourName\AppData\Local\PicList\PicList.exe
```

**macOS**:

```
/Applications/PicList.app/Contents/MacOS/PicList
```

**Linux**:

```
/usr/local/bin/PicList.AppImage
```

## 技术亮点

### 1. 自动切换上传方式

```java
// 根据配置自动选择最佳上传方式
if (StringUtils.isNotEmpty(exePath)) {
    // 使用命令行上传（更快，更适合本地）
    return uploadViaCommandLine(inputStream, fileName);
} else {
    // 使用 API 上传（更灵活，支持远程配置）
    return uploadViaApi(inputStream, fileName);
}
```

### 2. 平台兼容性

- ✅ Windows: `.exe` 文件
- ✅ macOS: `.app` 包
- ✅ Linux: `.AppImage` 文件

### 3. 完整的错误处理

- 超时保护（10秒）
- 退出码检查
- 剪贴板获取失败处理
- 临时文件清理

### 4. 文件过滤器

```java
descriptor.withFileFilter(virtualFile -> {
    String name = virtualFile.getName().toLowerCase();
    return name.endsWith(".exe") ||       // Windows
           name.endsWith(".app") ||       // macOS
           name.endsWith(".appimage");     // Linux
});
```

## 完成的 TODO

- ✅ 在 CloudEnum 中添加 PicList 枚举项
- ✅ 创建 PicListOssState 状态类
- ✅ 创建 PicListClient 客户端实现
- ✅ 创建 PicListOssSetting 设置类
- ✅ 在 MikState 中注册 PicListState
- ✅ 实现 API 上传功能
- ✅ 实现命令行上传功能
- ✅ 实现文件选择器（TextFieldWithBrowseButton）
- ✅ 实现从剪贴板获取结果
- ✅ 在 ProjectSettingsPage 中添加代码支持

## 待完成的工作

### UI 添加

需要在 IntelliJ 的 GUI Designer 中添加：

1. **PicList Tab**（index 8）
2. **picListAuthorizationPanel** JPanel
3. 所有配置字段的 UI 组件

详细步骤参见 `PICLIST_UI_SETUP.md`

## 测试建议

### API 上传测试

1. 确保 PicList 服务运行
2. 配置 API 接口地址
3. 点击 Test 按钮上传
4. 验证返回的 URL

### 命令行上传测试

1. 配置 PicList 可执行文件路径
2. 确保 PicList 已安装并可执行
3. 点击 Test 按钮上传
4. 验证从剪贴板获取的 URL

## 参考资料

- [PicList 官方文档](https://piclist.cn/advanced.html)
- [PicList API 文档](https://piclist.cn/advanced.html#api接口详解)
- [TextFieldWithBrowseButton 文档](docs/文件选择组件实现示例.md)
