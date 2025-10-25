# 更新日志

## 1.6.3

1. 添加水印

## 1.6.2

1. 国际化支持
2. 优化配置页
3. 移动插件设置到 Other 组
4. 修复百度密码获取错误的问题
5. 删除自定义的 JScrollPane, 删除 Configurable.NoScroll 接口
6. 使用消息框代替自定义消息

## 1.6.1

1. 删除七牛云 SDK, 使用 HttpClient 上传图片

## 1.6.0

1. 使用 Intellij Credentials API 代替 DES 加密, 因此需要重新设置密钥/Token,
   启动时会弹出 [认证信息](https://gitee.com/dong4j/idea-plugin-dev/raw/master/docs/product/imgs/MIK-C8LUI4.png)

   info.dong4j.idea.plugin.settings.oss.WeiboOssSetting info.dong4j.idea.plugin.settings.oss.AliyunOssSetting
   info.dong4j.idea.plugin.settings.oss.QiniuOssSetting info.dong4j.idea.plugin.settings.oss.TencentOssSetting
   info.dong4j.idea.plugin.settings.oss.BaiduBosSetting info.dong4j.idea.plugin.settings.oss.GithubSetting
   info.dong4j.idea.plugin.settings.oss.GiteeSetting

## 1.5.0-RELEASE

1. 添加自定义图床支持, <a href="https://github.com/dong4j/mik-help">API 示例</a>
2. 修复老版本更新后可能打不开设置页的问题
3. 修复敏感字段导致 IDEA Core 错误的问题

## 1.4.0

1. 上传到 Gitee
2. 修复 GitHub 主分支名错误的问题
3. 上传失败时不再替换原有的图片路径, 便于再次上传
4. 修复错误的鼠标监听器导致浏览器打开多个页面

## 1.3.0

1. 上传到 GitHub

## 1.2.0

1. 添加百度云图床

## 1.1.0

1. 移除 com.intellij.modules.java, 支持 WebStorm, PyCharm, GoLand 等 IDE
2. 重构依赖, 删除 apache 相关依赖, 减少插件体积

## 1.0.0

1. 删除 Qcloud SDK, 使用 API 上传图片
2. 删除 Aliyun SDK, 使用 API 上传图片
3. 修复 sm.ms 上传失败的问题
4. 修复批量上传警告

## 0.0.4

1. 修复 WebStrom 复制图片找不到 class 的问题

## 0.0.3

1. 支持腾讯云 COS
2. 优化 client 逻辑, 使用懒加载初始化 client
3. 修复 windows OS 下, 复制上传错误的问题
4. 修复持久化文件偶尔报错的问题, 此版本将持久化文件移动到单独的 markdown-image-kit.xml 文件中, 并做了加密处理. (可能需要根据报错信息, 删除 other.xml 中的对应节点)

## 0.0.2

1. 支持 sm.ms, 并设置为默认图床

## 0.0.1

提交第一个版本, 完成基础功能