package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.AbstractOpenOssSetting;
import info.dong4j.idea.plugin.settings.oss.AbstractOpenOssState;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Oss 客户端抽象类
 * <p>
 * 该类用于封装 Oss 客户端通用操作，提供上传文件的功能，并支持自定义端点和状态保存。
 * 主要用于实现不同 Oss 服务的客户端适配，如阿里云、腾讯云等。
 * <p>
 * 包含一些静态字段用于存储配置信息，如仓库名、分支名、Token、文件目录和是否使用自定义端点。
 * 提供抽象方法用于实现具体的上传逻辑、获取客户端和构建图片 URL。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public abstract class AbstractOpenClient implements OssClient {
    /** 仓库配置信息 */
    protected static String repos;
    /** 分支标识，用于标识当前处理的分支信息 */
    protected static String branch;
    /** AccessToken 值 */
    protected static String token;
    /** 文件存储目录路径 */
    protected static String filedir;
    /** 是否使用自定义端点 */
    protected static boolean isCustomEndpoint;
    /** 自定义端点地址，用于指定特定的服务接口地址 */
    protected static String customEndpoint;

    /**
     * 上传文件并返回文件访问地址
     * <p>
     * 通过输入流上传文件到指定路径，并根据配置返回对应的文件访问URL。
     *
     * @param inputStream 输入流，用于读取上传的文件内容
     * @param fileName    文件名，用于生成存储路径和访问地址
     * @return 文件的访问地址
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.6.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        String key = filedir + fileName;
        if (!key.startsWith("/")) {
            key = "/" + key;
        }

        this.putObjects(key, inputStream);

        if (isCustomEndpoint) {
            return "https://" + customEndpoint + key;
        }
        return this.buildImageUrl(key);
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用，用于获取当前配置并执行上传操作。
     * <p>
     * 该方法通过传入的 JPanel 获取用户输入的配置信息，包括仓库名、分支名、Token、文件目录等，并进行校验后调用上传方法。
     *
     * @param inputStream 输入流，用于读取上传文件的内容
     * @param fileName    文件名，表示要上传的文件名称
     * @param jPanel      用于获取配置信息的 JPanel 对象
     * @return 上传结果字符串
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.3.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String repos = map.get("repos");
        repos = AbstractOpenOssSetting.REPOS_HINT.equals(repos) ? "" : repos;
        String branch = map.get("branch");
        String token = map.get("token");
        String filedir = map.get("filedir");
        String customEndpoint = map.get("customEndpoint");
        boolean isCustomEndpoint = Boolean.parseBoolean(map.get("isCustomEndpoint"));

        Asserts.notBlank(repos, "仓库名");
        Asserts.notBlank(branch, "分支名");
        Asserts.notBlank(token, "Token");


        this.check(branch);

        return this.upload(inputStream,
                           fileName,
                           repos,
                           branch,
                           token,
                           filedir,
                           isCustomEndpoint,
                           customEndpoint);
    }

    /**
     * 检查指定的分支信息
     * <p>
     * 该方法用于验证传入的分支参数是否符合要求，若不符合则抛出异常
     *
     * @param branch 需要检查的分支名称
     * @throws IllegalArgumentException 如果分支参数无效
     */
    protected void check(String branch) {
    }

    /**
     * 处理文件上传请求，用于测试按钮点击事件或右键上传场景
     * <p>
     * 该方法接收文件输入流、文件名、仓库名、分支、令牌、文件目录等参数，构建客户端配置并执行上传操作。
     * 上传成功后，若返回URL不为空，会根据相关参数计算哈希值并更新OSS状态。
     *
     * @param inputStream      文件输入流
     * @param fileName         文件名
     * @param repos            仓库名称
     * @param branch           分支名称
     * @param token            访问令牌
     * @param filedir          文件目录
     * @param isCustomEndpoint 是否使用自定义端点
     * @param customEndpoint   自定义端点地址
     * @return 上传成功后的URL
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.3.0
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String repos,
                         String branch,
                         String token,
                         String filedir,
                         boolean isCustomEndpoint,
                         String customEndpoint) throws Exception {

        filedir = StringUtils.isBlank(filedir) ? "" : filedir + "/";

        AbstractOpenClient.repos = repos;
        AbstractOpenClient.filedir = filedir;
        // 主分支兼容处理
        AbstractOpenClient.branch = this.processBranch(branch);
        AbstractOpenClient.token = token;
        AbstractOpenClient.customEndpoint = customEndpoint;
        AbstractOpenClient.isCustomEndpoint = isCustomEndpoint;

        AbstractOpenClient client = this.getClient();

        String url = client.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = repos.hashCode() +
                           token.hashCode() +
                           branch.hashCode() +
                           (customEndpoint + isCustomEndpoint).hashCode();
            // 更新可用状态
            OssState.saveStatus(this.getState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }

    /**
     * 获取客户端实例
     * <p>
     * 返回当前客户端对象，用于与外部系统进行交互
     *
     * @return 客户端实例
     * @since 1.3.0
     */
    protected abstract AbstractOpenClient getClient();

    /**
     * 获取状态
     * <p>
     * 返回当前对象的状态实例，该状态用于表示对象的运行时状态。
     *
     * @return 当前对象的状态
     * @since 1.3.0
     */
    protected abstract AbstractOpenOssState getState();

    /**
     * 处理分支信息
     * <p>
     * 接收一个分支字符串参数，并直接返回该字符串。此方法主要用于传递或处理分支信息，不做额外处理。
     *
     * @param branch 分支名称或标识符
     * @return 返回传入的分支字符串
     */
    protected String processBranch(String branch) {
        return branch;
    }

    /**
     * 将对象数据存储到指定的键下
     * <p>
     * 该方法用于将输入流中的对象数据写入存储系统，具体实现由子类完成
     *
     * @param key      要存储数据的键
     * @param instream 包含对象数据的输入流
     * @throws Exception 存储过程中发生异常时抛出
     * @since 1.3.0
     */
    protected abstract void putObjects(String key, InputStream instream) throws Exception;

    /**
     * 构建图片的URL地址
     * <p>
     * 根据给定的键生成对应的图片URL字符串
     *
     * @param key 用于生成URL的键
     * @return 图片的URL字符串
     * @since 1.4.0
     */
    @NotNull
    protected abstract String buildImageUrl(String key);

}
