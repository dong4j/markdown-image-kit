# 自定义图床实现指南

本文档详细介绍了如何实现与 Markdown Image Kit 插件兼容的自定义图床服务，包括接口规范、示例代码和配置说明。

## 1. 接口规范

### 1.1 请求规范

插件会向您的服务发送一个 `multipart/form-data` 格式的 POST 请求，包含以下信息：

- **HTTP 方法**：默认为 POST（可在插件中配置）
- **Content-Type**：`multipart/form-data`
- **文件参数**：一个文件字段，参数名由用户在插件中配置

### 1.2 响应规范

您的服务需要返回 JSON 格式的响应，支持以下两种格式：

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

## 2. 示例实现

### 2.1 Node.js 实现

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
  
  // 构造可访问的 URL（请根据实际情况修改域名）
  const url = `http://your-domain.com/uploads/${req.file.filename}`;
  
  // 返回成功响应
  res.json({ url: url });
});

app.listen(3000, () => {
  console.log('图床服务运行在端口 3000');
});
```

### 2.2 Java Spring Boot 实现

```java
@RestController
@RequestMapping("/api/v1")
public class CustomOssController {
    
    private static final String UPLOAD_DIR = "uploads/";
    
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            response.put("error", "上传文件不能为空");
            response.put("code", 400);
            return response;
        }
        
        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + uniqueFilename);
            Files.write(path, bytes);
            
            // 构造访问 URL（请根据实际情况修改域名）
            String url = "http://your-domain.com/uploads/" + uniqueFilename;
            
            // 返回成功响应
            response.put("code", 200);
            response.put("url", url);
            return response;
            
        } catch (IOException e) {
            response.put("error", "文件上传失败: " + e.getMessage());
            response.put("code", 500);
            return response;
        }
    }
}
```

## 3. 插件配置说明

### 3.1 配置项说明

在 Markdown Image Kit 插件中配置自定义图床时，需要填写以下信息：

1. **API 地址**：您的图床服务上传接口地址
    - 示例：`http://localhost:3000/upload`

2. **请求 Key**：上传文件的参数名
    - 示例：`file`（与示例代码中的参数名保持一致）

3. **响应 URL 路径**：响应 JSON 中 URL 字段的路径
    - 如果响应为 `{ "url": "..." }`，填写：`url`
    - 如果响应为 `{ "data": { "url": "..." } }`，填写：`data.url`

4. **HTTP 方法**：请求方法
    - 通常为 `POST`

### 3.2 配置步骤

1. 打开 IntelliJ IDEA
2. 进入 `Preferences` → `Tools` → `Markdown Image Kit`
3. 选择 `OSS 设置` 选项卡
4. 选择 `自定义` 图床类型
5. 填写上述配置项
6. 点击 `测试` 按钮验证配置是否正确

## 4. 部署建议

### 4.1 安全性考虑

1. **文件类型验证**：限制上传文件的类型，只允许图片格式
2. **文件大小限制**：设置合理的文件大小上限
3. **访问控制**：考虑添加身份验证机制
4. **文件名处理**：使用安全的方式生成文件名，避免路径遍历攻击

### 4.2 性能优化

1. **CDN 加速**：使用 CDN 服务加速图片访问
2. **图片压缩**：在保存前对图片进行压缩处理
3. **缓存策略**：设置合适的 HTTP 缓存头

### 4.3 存储方案

1. **本地存储**：适合小型项目，简单易用
2. **云存储**：如 AWS S3、阿里云 OSS 等，适合大型项目
3. **分布式存储**：适合高并发场景

## 5. 故障排除

### 5.1 常见问题

1. **测试失败**
    - 检查 API 地址是否正确
    - 确认服务是否正常运行
    - 验证网络连接是否正常

2. **无法获取 URL**
    - 检查响应 URL 路径配置是否正确
    - 确认响应格式是否符合规范
    - 查看插件错误对话框中的详细信息

3. **上传成功但图片无法访问**
    - 检查文件权限设置
    - 确认 URL 构造是否正确
    - 验证静态资源服务配置

### 5.2 调试方法

1. **查看插件错误信息**：上传失败时，插件会显示详细的错误对话框
2. **检查服务日志**：查看您的图床服务日志
3. **使用网络调试工具**：如 Postman、curl 等工具测试接口

通过遵循以上规范和建议，您可以轻松实现与 Markdown Image Kit 插件兼容的自定义图床服务。