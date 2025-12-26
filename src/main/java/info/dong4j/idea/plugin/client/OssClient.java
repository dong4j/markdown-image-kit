package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikState;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OssClient 接口
 * <p>
 * 提供统一的 Oss 客户端操作接口，支持不同云服务商的客户端实现。通过静态内部类缓存客户端实例，实现单例模式管理。主要提供文件上传功能，支持基础上传和带界面组件的上传操作，适用于需要与云存储服务交互的场景。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public interface OssClient {
    /** 用于反射调用时的缓存，键为云服务类型，值为对应的OSS客户端实例，容量为实现类个数 */
    Map<CloudEnum, OssClient> INSTANCES = new ConcurrentHashMap<>(12);

    /**
     * 获取名称
     * <p>
     * 返回当前对象的名称，名称来源于云类型对象的 title 属性。
     *
     * @return 名称
     */
    default String getName(){
        return this.getCloudType().getTitle();
    }

    /**
     * 获取云类型
     * <p>
     * 返回当前系统的云类型枚举值
     *
     * @return 云类型，类型为 CloudEnum
     * @since 0.0.1
     */
    CloudEnum getCloudType();

    /**
     * 通过输入流和文件名上传文件
     * <p>
     * 该方法接收一个输入流和文件名，用于上传文件到指定位置
     *
     * @param inputStream 输入流，用于读取上传的文件内容
     * @param filename    文件名，表示上传文件的名称
     * @return 返回上传结果的字符串信息
     * @throws Exception 上传过程中发生异常时抛出
     */
    String upload(InputStream inputStream, String filename) throws Exception;

    /**
     * "Upload Test" 按钮测试上传（新接口）
     * <p>
     * 该方法用于执行"Upload Test"按钮的反射调用，接收输入流、文件名和MikState作为参数，从state中获取最新配置并执行上传。
     * 这是新的测试接口，优先使用此接口进行测试上传。
     *
     * @param inputStream 输入流，用于读取上传文件的数据
     * @param filename    文件名，表示上传文件的名称
     * @param state       MikState对象，包含所有配置状态信息
     * @return 处理结果字符串
     * @throws Exception 通用异常，用于封装可能发生的各种错误
     * @since 2.0.0
     */
    default String upload(InputStream inputStream, String filename, MikState state) throws Exception {
        // 默认实现：调用原有的 JPanel 方法（向后兼容）
        // 子类应该重写此方法以使用 state 参数
        throw new UnsupportedOperationException("该方法需要在具体的 OssClient 实现类中重写");
    }

}
