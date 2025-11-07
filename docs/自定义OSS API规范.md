# 自定义图床 API 规范

本文档定义了 Markdown Image Kit 插件自定义图床功能的 API 规范，旨在帮助开发者快速实现自己的图床服务。

## 1. 接口说明

### 1.1 接口地址

用户可在插件设置中自定义 API 地址。

### 1.2 请求方法

支持 GET、POST 等 HTTP 方法，默认为 POST。

### 1.3 Content-Type

请求的 Content-Type 为 `multipart/form-data`。

## 2. 请求参数

### 2.1 文件参数

| 参数名   | 类型   | 必填 | 说明                     |
|-------|------|----|------------------------|
| 用户自定义 | File | 是  | 上传的图片文件，参数名由用户在插件设置中指定 |

### 2.2 其他参数

插件目前不支持额外的文本参数，如有需要可在后续版本中添加。

## 3. 响应格式

### 3.1 成功响应

当上传成功时，API 应返回 HTTP 状态码 200，并在响应体中返回 JSON 格式的数据。

插件支持两种响应格式：

#### 格式一：直接返回 URL

```json
{
  "url": "https://your-domain.com/images/uploaded-image.jpg"
}
```

#### 格式二：嵌套对象中的 URL

```json
{
  "data": {
    "url": "https://your-domain.com/images/uploaded-image.jpg"
  }
}
```

用户可以在插件设置中指定响应中 URL 字段的路径，例如：

- `url` 表示 URL 在根节点
- `data.url` 表示 URL 在 data 对象内

### 3.2 错误响应

当上传失败时，建议返回非 200 的 HTTP 状态码，并在响应体中包含错误信息：

```json
{
  "error": "错误描述信息"
}
```

## 4. 示例实现

以下是一个简单的 Node.js Express 示例实现：

```javascript
const express = require('express');
const multer = require('multer');
const path = require('path');
const app = express();

// 配置 multer 存储
const storage = multer.diskStorage({
  destination: './uploads/',
  filename: function (req, file, cb) {
    cb(null, Date.now() + path.extname(file.originalname));
  }
});

const upload = multer({ storage: storage });

// 处理上传请求
app.post('/upload', upload.single('file'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: '没有上传文件' });
  }
  
  // 构造可访问的 URL
  const url = `http://your-domain.com/uploads/${req.file.filename}`;
  
  // 返回成功响应
  res.json({ url: url });
});

app.listen(3000, () => {
  console.log('图床服务运行在端口 3000');
});
```

## 5. 插件配置说明

在 Markdown Image Kit 插件中配置自定义图床时，需要填写以下信息：

1. **API 地址**：你的图床服务上传接口地址
2. **请求 Key**：上传文件的参数名（如示例中的 "file"）
3. **响应 URL 路径**：响应 JSON 中 URL 字段的路径（如 "url" 或 "data.url"）
4. **HTTP 方法**：请求方法（通常为 POST）

配置完成后，点击"测试"按钮验证配置是否正确。