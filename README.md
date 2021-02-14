# Markdown Image Kit

> 审核已通过, 可直接在 `plugin` 中搜索 `Markdown Image Kit` 下载

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/oDE4Cl.png)

`Markdown Image Kit` 是一款在 IDEA 中方便高效得管理 Markdown 文档图片的插件. 

在 IntelliJ IDEA 中写作(主要是技术文档), 配图成了一个大问题, 我们需要借助其他 APP 来完成这一操作.

为了解决现状, 因此开发了此插件, 能方便得给技术文档配图, 一键上传图片并直接替换为 markdown image 标签, 当然还提供其他一些附属功能.

## Features

1. 一键上传当前文档(所有文档)所有引用图片后自动替换, 体验最简单高效的一波流操作;
2. 支持多个图床, 还支持自定义图床, 没有你上传不了的图片;
3. 一键替换所有标签, 批量处理就是这么简单;
4. 粘贴图片, 复制就是 markdown image mark, 就是这么直接;
5. 图片直接压缩, 减少流量, 提高加载速度, 处处为你着想;
6. 可对一个 markdown image mark 单独处理, 灵活的不要不要的;
7. 图床迁移计划, 免费流量用完了? 迁移到另一个免费图床不就 ok 了;

## 功能演示

### 复制粘贴直接输出 image mark

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/save-image.gif)

### 复制粘贴直接上传到 OSS

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/paste-upload.gif)

### 复制本地图片直接上传

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/local-image-upload.gif)

### 单个标签上传

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/single-upload.gif)

### 批量上传

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/multi-upload.gif)

### 图床迁移

![](https://ws4.sinaimg.cn/large/eca7e314gy1g1k4n0baxyg212s0mqdp3.gif)

### 标签替换

![](https://ws2.sinaimg.cn/large/eca7e314gy1g1k4onvf0yg212s0mqdr0.gif)

### 上传到不同图床

![](https://ws1.sinaimg.cn/large/eca7e314gy1g1k4uynr3lg212s0mq4qp.gif)

## 详细设置

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/6PRHrK.png)

### Clipboard 监控

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/1mY3we.png)

如果开启了 `复制图片到目录`, 则会监控 Clipboard 中是否有 Image 类型的文件.

如果存在 Image 类型的文件, 直接使用 `粘贴` 操作即可将图片保存到指定目录.

如果开启了 `上传图片并替换`, 则在复制时将直接上传 clipboard 中的 Image 并替换为 markdown image mark.

> 作为最方便的一个功能, 可以只启用 `上传图片并替换`, 如果需要将图片备份到本地, 也可同时开启上面 2 个功能.

### OSS 设置

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/S5pISR.png)

第一版暂时只集成了 `微博图床`, `阿里云`, `七牛云`, 后期会慢慢集成其他图床.

只有正确设置认证信息且测试通过, 当前 OSS 才可用.

> 填完认证信息后, 一定要点 `Test Upload` 按钮测试认证信息.
> 每次修改了认证信息后也需要进行测试, 不然将不可用.

### 全局设置

![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/6bqMQc.png)

#### 设置默认图床

必须设置默认图床, clipboard 监控上传和 `alt + enter` 都是上传到默认图床.

> 第一版将 `微博` 初始化了为默认图床, 后期将使用 `sm.ms`, 不需要认证直接上传 

#### 替换标签

**此功能主要是自用**

如果将 markdown image mark 加上 `<a>`, 图片可点击并在新标签中打开.

如果你使用 vuepress 搭建博客, 可以使用 `点击看大图` 设置, 效果就是点击图片后即可放大图片

如果想要上面说的效果, 需要在 `config.js` 的 `head` 节点添加如下配置:

```javascript
// 让 Vuepress 支持图片放大功能
['script', { src: 'https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.slim.min.js' }],
['script', { src: 'https://cdnjs.cloudflare.com/ajax/libs/fancybox/3.5.2/jquery.fancybox.min.js' }],
['link', { rel: 'stylesheet', type: 'text/css', href: 'https://cdnjs.cloudflare.com/ajax/libs/fancybox/3.5.2/jquery.fancybox.min.css' }]
```

#### 图片压缩

开启后会在保存图片, 上传图片时压缩.

> gif 暂时未支持压缩, 因为 gif 是多帧图, 不好处理

#### 图片重命名

提供 3 中重命名方式, 但是 `微博图床` 不支持, 没有这个 API.

上传到 `微博图床` 后会返回一个 PID, URL 就是通过 PID 解析出来的,
如果加密方式改了, 这个插件就要升级了 (或者做个解析 PID 的 WEB 服务? `Help` 跳转到此页面就是这么做的).

## 其他

就这么几个功能, 用的 SDK 是 2018.3.5 的, 也不打算兼容老版本了, 能用就用吧.

如果你有什么想法或者新需求, 可以说出来听听, 万一实现了呢 😂.

欢迎提交 [issue](https://github.com/dong4j/markdown-image-kit/issues)

要是你在使用这款插件, 记得给我点个 [star](https://github.com/dong4j/markdown-image-kit)

## 修复报错

```
java.lang.Throwable: Element component@ImageManagerSetting.option.WeiboOssState.option.@name=password probably contains sensitive information (file: ~/Library/Preferences/IntelliJIdea2019.1/options/other.xml)
```

打开上面的文件, 删除 `<component name="ImageManagerSetting">` 节点

## TODO

1. 完成生效的图床集成
2. 添加 GitHub 图床
