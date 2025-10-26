# PicList 图床 UI 设置指南

## 概述

PicList 图床的核心功能已经完成，需要在 IntelliJ 的 GUI Designer 中添加 UI 组件。

## 已完成的代码

### 1. 后端代码

✅ `CloudEnum.java` - 添加了 PICLIST 枚举  
✅ `PicListOssState.java` - 状态管理  
✅ `PicListClient.java` - 客户端实现  
✅ `PicListOssSetting.java` - 设置逻辑  
✅ `MikState.java` - 状态注册  
✅ `OssState.java` - 状态查询  
✅ `ProjectSettingsPage.java` - 代码支持

### 2. 代码已经添加的支持

在 `ProjectSettingsPage.java` 中已经添加：

```java
// PicList UI 组件字段
private JPanel picListAuthorizationPanel;
private JTextField picListApiTextField;
private JTextField picListPicbedTextField;
private JTextField picListConfigNameTextField;
private JTextField picListKeyTextField;

// PicList 设置实例
private final PicListOssSetting picListOssSetting = new PicListOssSetting(
    this.picListApiTextField,
    this.picListPicbedTextField,
    this.picListConfigNameTextField,
    this.picListKeyTextField
);
```

并且在以下方法中已经添加了调用：

- `initAuthorizationTabbedPanel()` - 初始化
- `isModified()` - 检查修改
- `apply()` - 应用设置
- `reset()` - 重置设置

## 需要完成的 UI 工作

### 在 ProjectSettingsPage.form 中添加 PicList Tab

需要在 IntelliJ 中打开 `ProjectSettingsPage.form` 文件，然后：

#### 1. 添加 Tab 到 authorizationTabbedPanel

在 `authorizationTabbedPanel` 中添加一个新的 Tab，标题为 "PicList"（index 8）。

#### 2. 创建 picListAuthorizationPanel

在新添加的 Tab 中创建一个 `JPanel`，名为 `picListAuthorizationPanel`，布局建议使用 `GridBagLayout` 或类似布局。

#### 3. 添加以下 UI 组件

```xml
<!-- 在 picListAuthorizationPanel 中添加以下组件 -->

<!-- API 接口地址 -->
<JLabel text="API 接口地址:" name="picListApiLabel"/>
<JTextField name="picListApiTextField" size="400,30">
  <!-- 绑定到 picListApiTextField 字段 -->
</JTextField>

<!-- 图床类型 (可选) -->
<JLabel text="图床类型:" name="picListPicbedLabel"/>
<JTextField name="picListPicbedTextField" size="400,30"
  toolTipText="图床类型（如 aws-s3, qiniu 等）"/>

<!-- 配置名称 (可选) -->
<JLabel text="配置名称:" name="picListConfigNameLabel"/>
<JTextField name="picListConfigNameTextField" size="400,30"
  toolTipText="配置文件名称"/>

<!-- 密钥 (可选) -->
<JLabel text="密钥:" name="picListKeyLabel"/>
<JPasswordField name="picListKeyTextField" size="400,30"
  toolTipText="接口密钥（用于鉴权）"/>

<!-- 命令行路径 (可选，使用 TextFieldWithBrowseButton) -->
<JLabel text="命令行路径:" name="picListExeLabel"/>
<component class="com.intellij.openapi.ui.TextFieldWithBrowseButton" 
           binding="picListExeTextField" 
           size="400,30"
           toolTipText="PicList 可执行文件路径（留空则使用 API 上传）">
  <properties>
    <textFieldName value="picListExeTextField"/>
  </properties>
</component>
```

#### 4. 字段配置要求tcyun

| 字段名                          | 类型             | 必填 | 默认值                             | 说明       |
|------------------------------|----------------|----|---------------------------------|----------|
| `picListApiTextField`        | JTextField     | ✅  | `http://127.0.0.1:36677/upload` | API 接口地址 |
| `picListPicbedTextField`     | JTextField     | ❌  | 空                               | 图床类型     |
| `picListConfigNameTextField` | JTextField     | ❌  | 空                               | 配置名称     |
| `picListKeyTextField`        | JPasswordField | ❌  | 空                               | 密钥       |

### UI 布局建议

```
┌─────────────────────────────────────────┐
│ PicList                                 │
├─────────────────────────────────────────┤
│                                          │
│ API 接口地址:                           │
│ ┌─────────────────────────────────────┐ │
│ │ http://127.0.0.1:36677/upload       │ │
│ └─────────────────────────────────────┘ │
│                                          │
│ 图床类型（可选）:                       │
│ ┌─────────────────────────────────────┐ │
│ │ [留空使用默认]                      │ │
│ └─────────────────────────────────────┘ │
│                                          │
│ 配置名称（可选）:                       │
│ ┌─────────────────────────────────────┐ │
│ │ [留空使用默认]                      │ │
│ └─────────────────────────────────────┘ │
│                                          │
│ 密钥（可选）:                           │
│ ┌─────────────────────────────────────┐ │
│ │                                      │ │
│ └─────────────────────────────────────┘ │
│                                          │
└─────────────────────────────────────────┘
```

### 注意事项

1. **字段绑定**: 确保所有 UI 组件的名称与代码中声明的字段名称一致
2. **Tab 顺序**: PicList 的 Tab 应该是第 9 个（index 8），对应 `CloudEnum.PICLIST.index`
3. **组件类型**: `picListKeyTextField` 使用 `JPasswordField` 而不是 `JTextField`，用于隐藏输入
4. **初始化**: 代码已经处理了初始化逻辑，添加 Hint 提示

## 验证步骤

完成 UI 添加后，按以下步骤验证：

1. **编译项目**: 确保没有编译错误
2. **运行插件**: 启动插件并打开设置页面
3. **检查 Tab**: 确认可以看到 PicList Tab
4. **测试连接**: 使用 Test 按钮测试 PicList 连接
5. **保存配置**: 检查配置是否正确保存

## API 使用示例

PicList 客户端支持以下查询参数：

```
http://127.0.0.1:36677/upload?picbed=qiniu&configName=test&key=secret-key
```

- `picbed`: 指定图床类型（如 qiniu、aws-s3、tencent、aliyun 等）
- `configName`: 指定配置文件名称
- `key`: 用于接口鉴权的密钥

## 功能特性

✅ 支持 PicList 的高级功能  
✅ 灵活的配置参数  
✅ 完整的错误处理  
✅ 自动状态管理  
✅ 无缝集成到现有架构

## 参考资料

- [PicList 官方文档](https://piclist.cn/advanced.html)
- [PicList API 文档](https://piclist.cn/advanced.html#api接口详解)
