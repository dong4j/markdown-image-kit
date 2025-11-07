# Markdown Image Kit

<div align="center">

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/12192-markdown-image-kit?label=version)](https://plugins.jetbrains.com/plugin/12192-markdown-image-kit)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/d/12192-markdown-image-kit)](https://plugins.jetbrains.com/plugin/12192-markdown-image-kit)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Markdown Image Kit** 是一款专为 JetBrains IDE 设计的 Markdown 图片管理插件，让你在 IDE 中优雅地处理 Markdown 文档中的图片。

[English](README_EN.md) | [中文文档](README.md)

</div>

![](https://gitee.com/dong4j/idea-plugin-dev/raw/master/MIK-6RCoSs.png)

---

## 📖 目录

- [核心特性](#-核心特性)
- [支持的 IDE](#-支持的-ide)
- [快速开始](#-快速开始)
- [完整功能](#-完整功能)
- [详细配置](#-详细配置)
    - [图床配置](#1-图床配置)
    - [全局设置](#2-全局设置)
    - [剪贴板监控](#3-剪贴板监控)
- [图床集成策略](#-图床集成策略)
- [使用场景](#-使用场景)
- [实现亮点](#-实现亮点)
- [常见问题](#-常见问题)
- [贡献指南](#-贡献指南)
- [开源协议](#-开源协议)

---

## ✨ 核心特性

### 🎯 核心优势

- **🚀 一键上传** - 粘贴图片即自动上传，复制即是 Markdown 标签
- **🔄 图床迁移** - 轻松将图片从一个图床迁移到另一个
- **🔗 第三方集成** - 对接 PicList/PicGO 等成熟工具，聚焦核心功能
- **🎨 灵活配置** - 支持图片压缩、重命名、标签自定义等
- **📦 批量处理** - 一键处理文档中所有图片
- **💡 智能识别** - 自动识别本地/网络图片，精准处理

### 🌟 2.0 版本新特性

#### 🔄 图床集成策略调整

**从 OSS 直连到第三方工具对接的演进**

在早期版本中，本插件直接集成了多个对象存储服务商（阿里云 OSS、七牛云、腾讯云 COS 等）的 SDK。这种做法虽然功能强大，但也带来了一些问题：

- **插件体积膨胀** - 每集成一个图床就要引入一个 SDK
- **维护成本高** - 各厂商 API 变化时需要同步更新
- **覆盖面有限** - 无法支持所有用户的定制化需求

**2.0 版本的新思路**

我们将图床集成的职责转移给专业的第三方工具（如 PicList/PicGO），插件聚焦于核心能力：

```
旧方案: [MIK 插件] → [集成 SDK] → [阿里云/七牛/腾讯...]
新方案: [MIK 插件] → [PicList/PicGO] → [任意图床]
```

**优势明显**

- ✅ **插件更轻量** - 不再需要集成各种 SDK
- ✅ **支持更广泛** - PicList 支持 20+ 种图床，间接支持所有
- ✅ **用户可控** - 图床配置在 PicList 中管理，更灵活
- ✅ **职责分离** - 插件专注于 Markdown 图片标签处理

**无缝兼容**

保留了对常用图床的直接支持（阿里云、七牛、腾讯等），同时：

- 推荐使用 **PicList/PicGO** 获得最佳体验
- 对于特殊需求，可参考 [mik-help](https://github.com/dong4j/mik-help) 项目自建中转服务

---

## 🎮 支持的 IDE

支持所有基于 IntelliJ 平台的 IDE（2022.3 及以上版本）：

| IDE           | 支持版本                     |
|---------------|--------------------------|
| IntelliJ IDEA | Community & Ultimate     |
| PyCharm       | Professional & Community |
| WebStorm      | 全系列                      |
| PhpStorm      | 全系列                      |
| GoLand        | 全系列                      |
| Rider         | 全系列                      |
| CLion         | 全系列                      |
| RubyMine      | 全系列                      |
| DataGrip      | 全系列                      |
| RustRover     | 全系列                      |

---

## 🚀 快速开始

### 方式一：从插件市场安装（推荐）

1. 打开 IDE：`Settings/Preferences` → `Plugins`
2. 搜索 `Markdown Image Kit`
3. 点击 `Install` 安装
4. 重启 IDE

### 方式二：手动安装

1. 从 [Releases](https://github.com/dong4j/markdown-image-kit/releases) 下载最新版本
2. `Settings/Preferences` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. 选择下载的 zip 文件
4. 重启 IDE

### 基础配置

#### 推荐配置（使用 PicList）

1. **安装 PicList**
    - 访问 [PicList 官网](https://piclist.cn/) 下载安装
    - 或使用 PicGO（功能类似）

2. **配置 PicList**
    - 打开 PicList，配置你喜欢的图床（支持 20+ 种）
    - 启用 PicList Server（默认端口 36677）
    - 复制 API 地址：`http://127.0.0.1:36677/upload`

3. **配置插件**
    - IDE 中打开：`Settings/Preferences` → `Markdown Image Kit`
    - 选择 `PicList/PicGO` 图床
    - 填入 API 地址
    - 点击 `Test` 测试连接
    - 勾选 `设置为默认图床`

4. **开始使用**
    - 打开任意 Markdown 文件
    - 复制一张图片（截图/复制图片文件）
    - 粘贴到编辑器 → 自动上传 → 自动插入 Markdown 标签 🎉

#### 快速体验（使用 SM.MS 免费图床）

1. IDE 中打开：`Settings/Preferences` → `Markdown Image Kit`
2. **不勾选**「设置为默认图床」（使用内置的 SM.MS）
3. 剪贴板设置中勾选「上传图片并替换」
4. 复制图片 → 粘贴 → 自动上传到 SM.MS

> ⚠️ SM.MS 免费版有限制，建议直接使用 PicList, 可定制化选项更多.

---

## 🎯 完整功能

### 1. 图片上传

| 功能        | 说明           | 使用方式                                       |
|-----------|--------------|--------------------------------------------|
| **粘贴上传**  | 复制图片后直接粘贴    | `Ctrl/Cmd + V`                             |
| **单个上传**  | 对特定图片标签上传    | 光标置于标签上 → `Alt + Enter` → 选择上传选项           |
| **批量上传**  | 一键上传文档所有本地图片 | 右键 → `Markdown Image Kit` → `Upload Image` |
| **选择性上传** | 上传到不同图床      | 右键 → `Markdown Image Kit` → 选择目标图床         |

### 2. 图床迁移

从一个图床批量迁移到另一个：

```markdown
# 迁移前
![image](https://old-cdn.com/image.png)

# 迁移后  
![image](https://new-cdn.com/image.png)
```

**使用步骤**：

1. 配置目标图床
2. 右键 → `Markdown Image Kit` → `图床迁移`
3. 输入源图床域名（如 `old-cdn.com`）
4. 等待迁移完成

### 3. 标签替换

批量修改图片标签格式：

| 原始标签       | 替换后                                        | 适用场景           |
|------------|--------------------------------------------|----------------|
| `![](url)` | `<a href="url"><img src="url"/></a>`       | 图片可点击，新标签打开    |
| `![](url)` | `<img src="url" data-fancybox="gallery"/>` | VuePress 点击看大图 |
| 自定义模板      | 任意格式                                       | 个性化需求          |

### 4. 图片压缩

上传前自动压缩，减少存储和流量：

- 支持 PNG、JPG、JPEG、BMP 等格式
- 可调节压缩比例（0-100%）
- GIF 暂不支持（保留原始动图）

### 5. 图片重命名

| 重命名方式     | 格式              | 示例                          |
|-----------|-----------------|-----------------------------|
| **保持原名**  | 原文件名            | `screenshot.png`            |
| **日期+原名** | `yyyy-MM-dd-原名` | `2025-10-29-screenshot.png` |
| **随机字符**  | `MIK-随机6位`      | `MIK-a3Xk9p.png`            |

---

## ⚙️ 详细配置

打开设置：`Settings/Preferences` → `Markdown Image Kit`

### 1. 图床配置

#### PicList/PicGO（推荐） ⭐

**适用场景**：通用场景，一次配置，支持所有图床

**配置项**：

| 配置项        | 说明                  | 示例                              |
|------------|---------------------|---------------------------------|
| **API 地址** | PicList Server 地址   | `http://127.0.0.1:36677/upload` |
| **图床类型**   | PicList 配置的图床名称（选填） | `aws-s3`, `qiniu` 等             |
| **配置名称**   | 多配置时指定使用哪个（选填）      | `default`, `backup`             |
| **接口密钥**   | API 鉴权密钥（选填）        | 留空表示无需鉴权                        |

**优势**：

- ✅ 支持 20+ 种图床（阿里云、腾讯云、七牛云、AWS S3、MinIO、GitHub、Imgur...）
- ✅ 图床配置在 PicList 中管理，无需在插件中重复配置
- ✅ 支持图片压缩、重命名、水印等预处理
- ✅ 跨平台支持（Windows/macOS/Linux）

**详细文档**：[PicList 完整实现](docs/PICLIST_COMPLETE_IMPLEMENTATION.md)

#### 阿里云 OSS

**适用场景**：企业级应用，大流量场景

**配置项**：

- **Bucket Name**: 存储桶名称
- **Access Key**: AccessKey ID
- **Access Secret Key**: AccessKey Secret
- **Endpoint**: 地域节点（如 `oss-cn-beijing.aliyuncs.com`）
- **文件目录**: 存储路径前缀（如 `images/`）
- **自定义域名**: CDN 加速域名（选填）

**使用建议**：

- 建议配置 CDN 加速访问
- 设置 Bucket 访问权限为「公共读」
- 定期清理无用图片节省成本

#### 七牛云

**适用场景**：有免费额度，适合个人博客

**配置项**：

- **Bucket Name**: 存储空间名称
- **Access Key**: AccessKey
- **Secret Key**: SecretKey
- **上传地址**: 根据区域选择（华东/华北/华南/北美）
- **自定义域名**: 绑定的域名（必填）

**免费额度**：每月 10GB 存储 + 10GB 流量

#### 腾讯云 COS

**适用场景**：腾讯云生态，与其他腾讯云服务集成

**配置项**：

- **Bucket Name**: 存储桶名称（格式：`bucketname-appid`）
- **Secret ID**: 密钥 ID
- **Secret Key**: 密钥内容
- **Region**: 地域（如 `ap-beijing`）

#### GitHub

**适用场景**：免费方案，适合开源项目文档

**配置项**：

- **Repository**: 仓库名（格式：`username/repo`）
- **Branch**: 分支名（如 `main` 或 `gh-pages`）
- **Token**: Personal Access Token（需要 `repo` 权限）
- **文件目录**: 存储路径（如 `images/`）
- **自定义域名**: 使用 jsDelivr CDN 加速（选填）

**CDN 加速示例**：

```
原始地址: https://raw.githubusercontent.com/user/repo/main/image.png
CDN 地址: https://cdn.jsdelivr.net/gh/user/repo@main/image.png
```

#### Gitee

**适用场景**：国内访问速度快，适合国内用户

配置类似 GitHub，使用 Gitee 作为存储

#### 自定义图床

**适用场景**：使用非主流图床，或自建图床服务

**配置项**：

- **API 地址**: 上传接口 URL
- **请求密钥**: 文件参数名（如 `file`, `image`）
- **响应路径**: JSON 响应中 URL 的路径（如 `data.url`）
- **HTTP 方法**: 通常为 `POST`

**自建图床示例**：

参考 [mik-help](https://github.com/dong4j/mik-help) 项目，提供了多语言实现：

```bash
# Java 实现
cd mik-help/upload-api/java
mvn spring-boot:run

# Node.js 实现  
cd mik-help/upload-api/nodejs
npm install && npm start

# Python 实现
cd mik-help/upload-api/python  
pip install -r requirements.txt && python app.py
```

**API 规范**：

```bash
# 请求
POST /upload
Content-Type: multipart/form-data

# 响应
{
  "data": {
    "url": "https://your-domain.com/image.png"
  }
}
```

详细说明：

- [自定义图床 API 规范](docs/CUSTOM_OSS_API_SPECIFICATION.md)
- [自定义图床实现指南](docs/CUSTOM_OSS_IMPLEMENTATION_GUIDE.md)

---

### 2. 全局设置

#### 默认图床

**说明**：勾选后，粘贴图片将自动上传到选中的图床

**配置**：

- ☑️ **设置为默认图床**
- 选择默认上传的图床类型

**使用场景**：

- ✅ 日常写作，希望粘贴即上传
- ✅ 团队协作，统一使用同一图床

**不勾选时**：使用内置的 SM.MS 免费图床

#### 标签替换

**说明**：将 Markdown 原始标签替换为其他格式

**选项**：

| 选项        | 生成格式                                       | 适用场景            |
|-----------|--------------------------------------------|-----------------|
| **正常**    | `<a href="url"><img src="url"/></a>`       | 图片可点击在新标签打开     |
| **点击看大图** | `<img src="url" data-fancybox="gallery"/>` | VuePress 博客点击放大 |
| **自定义**   | 自定义模板                                      | 特殊需求            |

**自定义模板语法**：

```
{{0}} - 图片标题（title）
{{1}} - 图片地址（URL）

示例: <img alt="{{0}}" src="{{1}}" style="max-width:100%"/>
```

**VuePress 配置**（点击看大图）：

在 `config.js` 的 `head` 节点添加：

```javascript
['script', { src: 'https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.slim.min.js' }],
['script', { src: 'https://cdnjs.cloudflare.com/ajax/libs/fancybox/3.5.2/jquery.fancybox.min.js' }],
['link', { rel: 'stylesheet', type: 'text/css', href: 'https://cdnjs.cloudflare.com/ajax/libs/fancybox/3.5.2/jquery.fancybox.min.css' }]
```

#### 图片压缩

**说明**：上传前自动压缩图片，减少存储和流量

**配置**：

- ☑️ **开启压缩**
- 滑动调节压缩比例（0-100，推荐 60-80）

**压缩效果示例**：

| 原始大小   | 压缩比例 | 压缩后    | 压缩率 |
|--------|------|--------|-----|
| 2.5 MB | 60%  | 800 KB | 68% |
| 1.2 MB | 70%  | 600 KB | 50% |
| 500 KB | 80%  | 350 KB | 30% |

**注意事项**：

- ✅ 支持 PNG、JPG、JPEG、BMP
- ❌ GIF 暂不支持（会保留原始动图）
- 压缩不可逆，建议保留原图备份

**使用场景**：

- 博客文章配图（减少加载时间）
- 移动端访问优化
- 流量成本控制

#### 文件重命名

**说明**：上传时重命名文件，避免冲突或规范命名

**选项**：

| 方式        | 格式              | 示例                          | 适用场景    |
|-----------|-----------------|-----------------------------|---------|
| **保持原名**  | 原文件名            | `screenshot.png`            | 文件名有意义时 |
| **日期+原名** | `yyyy-MM-dd-原名` | `2025-10-29-screenshot.png` | 按日期归档   |
| **随机字符**  | `MIK-随机6位`      | `MIK-a3Xk9p.png`            | 避免重名冲突  |

**使用建议**：

- 推荐使用「随机字符」避免文件名冲突
- 「日期+原名」适合按时间管理图片
- 「保持原名」适合文件名本身有意义的场景

#### 水印

**说明**：上传前为图片添加文字水印

**配置**：

- ☑️ **开启水印**
- 输入水印文字（如 `@YourName`）

**注意**：当前版本水印功能基础，复杂需求建议使用 PicList 的水印插件

---

### 3. 剪贴板监控

**说明**：监控剪贴板，自动处理复制的图片

#### 复制图片到目录

**说明**：复制图片时自动保存到本地目录

**配置**：

- ☑️ **复制图片到目录**
- 设置保存路径（相对于当前文档，如 `./imgs`）

**工作流程**：

```
复制图片 → 粘贴 → 保存到 ./imgs → 插入相对路径标签
结果: ![](./imgs/image.png)
```

**使用场景**：

- ✅ 图片需要本地备份
- ✅ 离线文档编辑
- ✅ Git 仓库管理文档

#### 上传图片并替换

**说明**：复制图片时自动上传到图床

**配置**：

- ☑️ **上传图片并替换**

**工作流程**：

```
复制图片 → 粘贴 → 自动上传 → 插入网络 URL
结果: ![](https://cdn.example.com/image.png)
```

**使用场景**：

- ✅ 博客写作（图片托管到 CDN）
- ✅ 在线文档（图片公网可访问）
- ✅ 多平台发布（复制 Markdown 即可用）

#### 组合使用

两个选项可以**同时勾选**：

```
复制图片 → 粘贴 → 保存本地 + 上传图床 → 插入网络 URL
```

**优势**：

- 本地有备份
- 对外分享用图床 URL
- 图床迁移有原始文件

---

## 🔄 图床集成策略

### 从 SDK 集成到工具对接的演进

#### 1.x 版本：直接集成 OSS SDK

**架构**：

```
[Markdown Image Kit 插件]
    ├── 阿里云 OSS SDK
    ├── 七牛云 SDK  
    ├── 腾讯云 COS SDK
    ├── 百度云 BOS SDK
    ├── GitHub API
    └── Gitee API
```

**问题**：

- ❌ 插件体积大（每个 SDK 都有几 MB）
- ❌ 维护成本高（厂商 API 变化需要同步更新）
- ❌ 无法覆盖所有图床（用户可能用 MinIO、WebDAV 等）

#### 2.x 版本：对接第三方工具

**新架构**：

```
[Markdown Image Kit 插件]
    └── PicList/PicGO API
            └── [PicList 工具]
                    ├── 阿里云 OSS
                    ├── 腾讯云 COS
                    ├── 七牛云
                    ├── AWS S3
                    ├── MinIO
                    ├── GitHub
                    ├── Imgur
                    ├── SM.MS
                    └── ... 20+ 种图床
```

**优势**：

- ✅ 插件体积减小 80%
- ✅ 支持 20+ 种图床（间接支持）
- ✅ 图床配置在 PicList 管理，更灵活
- ✅ 插件聚焦于 Markdown 图片标签处理
- ✅ PicList 生态强大（插件、主题、预设）

### 三种使用模式

#### 模式 1：直接使用内置图床（最简单）

```
[插件] → [内置 SM.MS] → [免费图床]
```

**适合**：快速体验、临时使用

#### 模式 2：通过 PicList 对接图床（推荐）

```
[插件] → [PicList API] → [你配置的任意图床]
```

**适合**：生产环境、长期使用

**优势**：

- PicList 支持 20+ 种图床
- 配置一次，多处使用（浏览器插件、命令行工具等）
- 图床迁移只需在 PicList 切换配置

#### 模式 3：自建图床服务（高级）

```
[插件] → [自定义 API] → [你的处理逻辑] → [任意存储]
```

**适合**：特殊需求、企业内部

**示例场景**：

1. **图片预处理**：上传前压缩、添加水印、格式转换
2. **中转服务**：统一入口，后端可切换不同图床
3. **权限控制**：企业内部图床，需要鉴权
4. **特殊存储**：MinIO、WebDAV、NAS 等

**实现参考**：[mik-help](https://github.com/dong4j/mik-help)

提供了多语言实现：

- ☕ Java (Spring Boot)
- 🟢 Node.js (Express)
- 🐍 Python (Flask)
- 🐹 Go (Gin)
- 💎 Kotlin (Ktor)

每个实现都包含：

- 文件上传 API
- 静态资源预览
- 详细部署文档
- Docker 镜像

---

## 📚 使用场景

### 场景 1：技术博客写作

**需求**：

- 截图后快速插入文章
- 图片托管到 CDN 加速访问
- 多平台发布（掘金、CSDN、知乎等）

**配置方案**：

1. 使用 PicList + 七牛云（有免费额度）
2. 勾选「上传图片并替换」
3. 开启图片压缩（70%）

**工作流**：

```
截图 → 粘贴 → 自动上传 → 插入 CDN 链接 → 完成 🎉
```

### 场景 2：团队文档协作

**需求**：

- 文档存储在 Git 仓库
- 图片需要本地备份
- 对外分享时图片公网可访问

**配置方案**：

1. 同时勾选「复制图片到目录」和「上传图片并替换」
2. 设置保存路径为 `./assets/images`
3. 使用阿里云 OSS（企业级稳定）

**优势**：

- 本地有备份（提交到 Git）
- 分享时用图床 URL（公网访问）
- 图床迁移有原始文件

### 场景 3：开源项目文档

**需求**：

- 免费方案
- 全球访问速度
- 图片永久有效

**配置方案**：

1. 使用 PicList + GitHub
2. 开启 jsDelivr CDN 加速
3. 图片存储在 `gh-pages` 分支

**工作流**：

```
上传图片 → GitHub 仓库 → jsDelivr CDN → 全球加速
URL: https://cdn.jsdelivr.net/gh/user/repo@main/image.png
```

### 场景 4：图床迁移

**需求**：

- 原图床流量用完了
- 需要迁移到新图床
- 批量处理文档中的图片

**操作步骤**：

1. 配置新图床并设为默认
2. 打开文档，右键 → `图床迁移`
3. 输入旧图床域名（如 `old-cdn.com`）
4. 等待迁移完成

**原理**：

- 识别所有包含旧域名的图片
- 下载原图 → 上传到新图床 → 替换 URL

### 场景 5：离线文档编辑

**需求**：

- 在飞机/高铁上写文档
- 没有网络连接
- 回到办公室后批量上传

**配置方案**：

1. 只勾选「复制图片到目录」
2. 截图粘贴时保存到本地

**回到办公室后**：

1. 右键 → `Markdown Image Kit` → `Upload Image`
2. 一键上传文档中所有本地图片
3. 自动替换为图床 URL

---

## 💡 实现亮点

### 1. 责任链模式处理图片任务

插件使用**责任链模式**（Chain of Responsibility Pattern）优雅地处理图片上传的完整流程。

#### 为什么使用责任链？

图片上传不是一个简单操作，而是一系列步骤的组合：

```
解析 Markdown → 压缩图片 → 重命名 → 上传 → 标签转换 → 写回文档
```

如果用传统的顺序调用：

```java
// ❌ 传统写法：代码耦合，难以维护
void uploadImage() {
    parseMarkdown();
    if (needCompress) compressImage();
    if (needRename) renameImage();
    uploadToOss();
    if (needChangeTag) changeTag();
    writeToDocument();
}
```

**问题**：

- 每个步骤都是 if 判断，逻辑混乱
- 新增功能需要修改主流程
- 难以复用、难以测试

#### 责任链模式的实现

**核心接口**：

```java
public interface IActionHandler {
    String getName();                    // 处理器名称
    boolean isEnabled(EventData data);   // 是否启用
    boolean execute(EventData data);     // 执行逻辑
}
```

**处理器示例**：

```1:93:markdown-image-kit/src/main/java/info/dong4j/idea/plugin/chain/ImageCompressionHandler.java
package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片压缩处理类
 * <p>
 * 用于处理 Markdown 图片的压缩操作，将图片的 InputStream 进行压缩处理，并更新为压缩后的输入流。
 * 支持对非 GIF 格式的图片进行压缩，同时记录压缩前后的大小及压缩率。
 * 该类继承自 ActionHandlerAdapter，用于在特定事件触发时执行图片压缩逻辑。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ImageCompressionHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示操作进度标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.progress.title");
    }

    /**
     * 判断当前状态是否启用压缩功能
     * <p>
     * 根据当前状态判断是否启用压缩功能，返回对应的布尔值
     *
     * @param data 事件数据，用于上下文信息
     * @return 是否启用压缩功能
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return IntentionActionBase.getState().isCompress();
    }

    /**
     * 处理Markdown图片数据，压缩图片流并更新图片对象
     * <p>
     * 该方法用于处理Markdown图片数据，首先检查图片流是否为空，若为空则从迭代器中移除该图片。
     * 若图片名称以"gif"结尾，则直接返回。否则，使用ImageUtils工具类对图片进行压缩处理，并将压缩后的流设置回图片对象。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历和移除图片
     * @param markdownImage Markdown图片对象，包含图片名称和输入流
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();
        if (markdownImage.getInputStream() == null) {
            log.trace("inputstream 为 null, remove markdownImage = {}", markdownImage);
            imageIterator.remove();
            return;
        }

        if (imageName.endsWith("gif")) {
            return;
        }

        InputStream inputStream = markdownImage.getInputStream();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageUtils.compress(inputStream, outputStream, IntentionActionBase.getState().getCompressBeforeUploadOfPercent());
            markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            log.trace("", e);
        }
    }
```

**构建处理链**：

```119:144:markdown-image-kit/src/main/java/info/dong4j/idea/plugin/chain/ActionManager.java
    /**
     * 构建上传流程的动作管理器
     * <p>
     * 根据传入的事件数据创建一个包含多个处理步骤的动作管理器，用于处理文件上传的完整流程。
     *
     * @param data 事件数据
     * @return 动作管理器实例
     * @since 0.0.1
     */
    public static ActionManager buildUploadChain(EventData data) {
        return new ActionManager(data)
            // 解析 markdown 文件
            .addHandler(new ResolveMarkdownFileHandler())
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 处理 client
            .addHandler(new OptionClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new ReplaceToDocument())
            .addHandler(new FinalChainHandler());
    }
```

**执行流程**：

```93:116:markdown-image-kit/src/main/java/info/dong4j/idea/plugin/chain/ActionManager.java
    /**
     * 执行处理链中的各个处理器
     * <p>
     * 遍历处理器链，依次调用每个启用的处理器，并更新进度指示器的状态
     *
     * @param indicator 进度指示器，用于显示处理进度和当前处理的处理器名称
     */
    public void invoke(ProgressIndicator indicator) {
        int totalProcessed = 0;
        this.data.setIndicator(indicator);
        this.data.setSize(this.handlersChain.size());
        int index = 0;
        for (IActionHandler handler : this.handlersChain) {
            this.data.setIndex(index++);
            if (handler.isEnabled(this.data)) {
                log.trace("invoke {}", handler.getName());
                indicator.setText2(handler.getName());
                if (!handler.execute(this.data)) {
                    break;
                }
            }
            indicator.setFraction(++totalProcessed * 1.0 / this.handlersChain.size());
        }
    }
```

#### 优势

**1. 开闭原则**

新增功能只需添加新的 Handler，无需修改现有代码：

```java
// ✅ 新增水印功能
public class WatermarkHandler extends ActionHandlerAdapter {
    @Override
    public boolean isEnabled(EventData data) {
        return state.isWatermark();
    }
    
    @Override
    public void invoke(...) {
        // 添加水印逻辑
    }
}

// 只需在链中添加
.addHandler(new WatermarkHandler())
```

**2. 单一职责**

每个 Handler 只负责一件事：

- `ImageCompressionHandler` - 只负责压缩
- `ImageRenameHandler` - 只负责重命名
- `ImageUploadHandler` - 只负责上传

**3. 灵活配置**

通过 `isEnabled()` 方法动态启用/禁用处理器：

```java
@Override
public boolean isEnabled(EventData data) {
    return state.isCompress();  // 用户勾选才执行
}
```

**4. 易于测试**

每个 Handler 可以单独测试：

```java
@Test
void testImageCompression() {
    ImageCompressionHandler handler = new ImageCompressionHandler();
    EventData data = createTestData();
    handler.execute(data);
    // 验证压缩效果
}
```

**5. 可观测性**

处理链执行时可以追踪每个步骤：

```
[进度] 解析 Markdown 文件...
[进度] 压缩图片...  
[进度] 重命名文件...
[进度] 上传到图床...
[进度] 转换标签...
[进度] 写入文档...
```

### 2. 并发上传优化

图片上传支持多线程并发，大幅提升批量处理速度：

```74:134:markdown-image-kit/src/main/java/info/dong4j/idea/plugin/chain/ImageUploadHandler.java
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();

        // 统计总数，用于进度计算
        int totalCount = data.getWaitingProcessMap().values().stream()
            .mapToInt(List::size)
            .sum();

        // 使用原子变量跟踪进度，确保线程安全
        AtomicInteger processedCount = new AtomicInteger(0);

        // 动态计算线程池大小，最多使用10个线程，但要考虑图片数量
        int threadPoolSize = Math.min(Math.max(totalCount, 2), 10);
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        log.info("开始上传 {} 张图片，使用 {} 个线程", totalCount, threadPoolSize);

        List<CompletableFuture<?>> futures = new ArrayList<>();

        // 收集所有需要处理的图片
        List<ImageUploadTask> uploadTasks = new ArrayList<>();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                uploadTasks.add(new ImageUploadTask(markdownImage, imageEntry.getValue()));
            }
        }

        // 为每个图片创建异步任务
        for (ImageUploadTask task : uploadTasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    MarkdownImage markdownImage = task.markdownImage;
                    int currentProcessed = processedCount.incrementAndGet();

                    // 更新进度
                    String imageName = markdownImage.getImageName();
                    indicator.setText2(MikBundle.message("mik.action.processing.title", imageName));
                    indicator.setFraction(((currentProcessed * 1.0) + data.getIndex() * size) / (totalCount * size));

                    // 执行上传逻辑
                    uploadImage(data, task.imageIterator, markdownImage);
                } catch (Exception e) {
                    log.error("上传图片时发生异常: {}", task.markdownImage.getImageName(), e);
                }
            }, executorService);

            futures.add(future);
        }

        // 等待所有任务完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).join();
            log.info("图片上传完成，共处理 {} 张图片", totalCount);
        } finally {
            executorService.shutdown();
        }

        return true;
    }
```

**性能提升**：

- 单线程：10 张图片 ≈ 30 秒
- 并发上传：10 张图片 ≈ 5 秒

### 3. 智能图片识别

自动识别图片来源（本地/网络），避免重复上传：

```167:218:markdown-image-kit/src/main/java/info/dong4j/idea/plugin/chain/ImageUploadHandler.java
    /**
     * 上传图片的核心逻辑
     *
     * @param data          事件数据
     * @param imageIterator 图片迭代器
     * @param markdownImage 待上传的图片
     */
    private void uploadImage(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();

        // 已上传过的不处理
        if (ImageLocationEnum.NETWORK.equals(markdownImage.getLocation())) {
            log.debug("图片 {} 已经上传过，跳过", imageName);
            return;
        }

        // 验证图片数据
        if (StringUtils.isBlank(imageName) || markdownImage.getInputStream() == null) {
            log.warn("图片名称或输入流为空，移除该图片: {}", markdownImage);
            imageIterator.remove();
            return;
        }

        String imageUrl = null;
        Exception uploadException = null;

        try {
            log.debug("开始上传图片: {}", imageName);
            imageUrl = data.getClient().upload(markdownImage.getInputStream(), markdownImage.getImageName());
            log.debug("图片上传成功: {} -> {}", imageName, imageUrl);
        } catch (Exception e) {
            uploadException = e;
            log.error("上传图片失败: {}, 错误信息: {}", imageName, e.getMessage(), e);
        }

        // 更新图片信息
        String mark;
        if (StringUtils.isBlank(imageUrl)) {
            mark = "![upload error](" + markdownImage.getPath() + ")";
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            log.warn("图片 {} 上传失败，保留为本地路径", imageName);
        } else {
            mark = "![](" + imageUrl + ")";
            markdownImage.setPath(imageUrl);
            markdownImage.setLocation(ImageLocationEnum.NETWORK);
        }

        markdownImage.setOriginalLineText(mark);
        markdownImage.setOriginalMark(mark);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
        markdownImage.setFinalMark(mark);
    }
```

### 4. 灵活的标签解析

支持多种 Markdown 图片标签格式：

```markdown
![](image.png)
![title](image.png)
![](./imgs/image.png)
![](https://cdn.example.com/image.png)
<img src="image.png"/>
```

### 5. 完善的错误处理

- 上传失败时保留原始标签，不会丢失数据
- 详细的日志记录，便于排查问题
- 友好的错误提示

---

## ❓ 常见问题

### 1. 如何配置 PicList？

**Q**: 第一次使用 PicList，如何配置？

**A**:

1. 下载安装 [PicList](https://piclist.cn/)
2. 打开 PicList，进入「图床设置」
3. 选择你要用的图床（如阿里云 OSS）
4. 填入认证信息，点击「确定」
5. 打开「PicGo 设置」→「设置 Server」→ 勾选「开启 Server」
6. 复制 Server 地址：`http://127.0.0.1:36677/upload`
7. 在插件中配置此地址

### 2. 图片上传失败怎么办？

**Q**: 粘贴图片后上传失败，如何排查？

**A**:

1. **检查网络**：确保能访问图床
2. **测试连接**：在设置页面点击「Test」按钮
3. **查看日志**：
    - IDE 中：`Help` → `Show Log in Finder/Explorer`
    - 搜索 `mik` 关键词查看错误
4. **检查配置**：认证信息是否正确
5. **PicList 日志**：如果用 PicList，查看其日志

### 3. 能否批量上传已有文档的图片？

**Q**: 我有很多旧文档，图片都是本地路径，如何批量上传？

**A**:

1. 配置好图床
2. 打开文档
3. 右键 → `Markdown Image Kit` → `Upload Image`
4. 等待处理完成

### 4. 如何迁移图床？

**Q**: 原来的图床不想用了，如何迁移到新图床？

**A**:

1. 配置新图床并设为默认
2. 打开文档
3. 右键 → `Markdown Image Kit` → `图床迁移`
4. 输入旧图床域名（如 `old-cdn.com`）
5. 等待迁移完成

### 5. 图片压缩会降低质量吗？

**Q**: 担心图片压缩后质量太差？

**A**:

- 压缩算法优化过，肉眼几乎无差别
- 可调节压缩比例（推荐 60-80）
- 如果在意质量，可以：
    - 关闭压缩功能
    - 使用 PicList 的图片压缩插件（更多选项）

### 6. SM.MS 免费图床有什么限制？

**Q**: 默认的 SM.MS 图床好用吗？

**A**:

- ✅ 完全免费
- ✅ 无需注册
- ❌ 单张图片限制 5MB
- ❌ 可能有上传频率限制
- ❌ 免费版无法删除图片

**建议**：体验可以，生产环境建议用 PicList + 自己的图床

### 7. 支持哪些图片格式？

**A**:

- ✅ PNG
- ✅ JPG/JPEG
- ✅ GIF（压缩功能不支持 GIF）
- ✅ BMP
- ✅ WebP

### 8. 如何备份图片？

**Q**: 担心图床挂了图片丢失？

**A**:

1. 同时勾选「复制图片到目录」和「上传图片并替换」
2. 图片会保存到本地 + 上传到图床
3. 本地文件可以提交到 Git 仓库
4. 图床挂了可以重新上传

### 9. 插件会上传私密图片吗？

**Q**: 图片隐私安全吗？

**A**:

- ❌ 插件**不会**自动上传任何图片
- ✅ 只有在你**主动粘贴**或**点击上传**时才会上传
- ✅ 图片直接上传到你配置的图床
- ✅ 插件不会收集任何数据

如果担心隐私：

- 使用「复制图片到目录」功能，不上传
- 或者自建本地图床服务（参考 mik-help）

### 10. 遇到 Bug 如何反馈？

**A**:

1. GitHub Issues: [提交 Issue](https://github.com/dong4j/markdown-image-kit/issues)
2. 提供信息：
    - IDE 版本
    - 插件版本
    - 操作步骤
    - 错误日志
    - 截图

---

## 🤝 贡献指南

欢迎各种形式的贡献！

### 报告 Bug

[提交 Issue](https://github.com/dong4j/markdown-image-kit/issues/new?template=bug_report.md)

### 功能建议

[提交 Feature Request](https://github.com/dong4j/markdown-image-kit/issues/new?template=feature_request.md)

### 代码贡献

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 本地开发

```bash
# 克隆仓库
git clone https://github.com/dong4j/markdown-image-kit.git
cd markdown-image-kit/markdown-image-kit

# 构建项目
./gradlew buildPlugin

# 运行插件（会启动一个带插件的 IDE 实例）
./gradlew runIde
```

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源。

---

## 🔗 相关链接

- [JetBrains 插件市场](https://plugins.jetbrains.com/plugin/12192-markdown-image-kit)
- [GitHub 仓库](https://github.com/dong4j/markdown-image-kit)
- [配套服务 mik-help](https://github.com/dong4j/mik-help)
- [PicList 官网](https://piclist.cn/)
- [问题反馈](https://github.com/dong4j/markdown-image-kit/issues)

---

## 👤 作者

**dong4j**

- Email: dong4j@gmail.com
- GitHub: [@dong4j](https://github.com/dong4j)

---

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

如果这个插件对你有帮助，请给一个 ⭐️ Star 支持一下！

---

<div align="center">

**Enjoy Markdown writing! 📝**

Made with ❤️ by dong4j

</div>
