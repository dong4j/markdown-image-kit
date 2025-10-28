package info.dong4j.idea.plugin.content;

/**
 * MikContents 接口
 * <p>
 * 定义与 MikroORM 相关的常量内容，主要用于标识和处理特定文件或目录结构。
 * 该接口提供了一个常量用于表示 Node.js 的模块目录名称。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public interface MikContents {
    /** node_modules 文件夹名称，用于标识 Node.js 项目依赖库目录 */
    String NODE_MODULES_FILE = "node_modules";

    /** 帮助 REST API URL */
    String HELP_REST_URL_KEY = "http://rest.dong4j.info/mik/help/";
}
