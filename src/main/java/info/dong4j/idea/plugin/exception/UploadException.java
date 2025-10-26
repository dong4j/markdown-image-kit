package info.dong4j.idea.plugin.exception;

import com.intellij.openapi.project.Project;

import java.io.Serial;
import java.util.function.Supplier;

/**
 * 上传异常类
 * <p>
 * 用于封装上传过程中发生的异常信息，包含项目信息和异常消息。该类继承自 RuntimeException，
 * 并实现了 Supplier 接口，用于提供 UploadException 实例。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public class UploadException extends RuntimeException implements Supplier<UploadException> {
    /** 序列化版本号，用于确保类的兼容性 */
    @Serial
    private static final long serialVersionUID = 4076461843028836262L;
    /** 项目对象，用于表示当前操作的项目信息 */
    private Project project;

    /**
     * 创建一个 UploadException 实例
     * <p>
     * 无参数构造函数，用于初始化 UploadException 异常对象
     *
     * @since 0.0.1
     */
    public UploadException() {
        super();
    }

    /**
     * 上传异常类
     * <p>
     * 表示上传过程中发生的异常情况
     *
     * @param message 异常信息
     * @since 0.0.1
     */
    public UploadException(String message) {
        super(message);
    }

    /**
     * 上传异常类的构造函数
     * <p>
     * 初始化一个 UploadException 实例，并设置对应的项目信息
     *
     * @param project 项目对象，用于标识异常发生的项目
     * @since 0.0.1
     */
    public UploadException(Project project) {
        super();
        this.project = project;
    }

    /**
     * 根据项目和消息创建上传异常
     * <p>
     * 该构造函数用于初始化一个上传异常对象，指定异常发生的项目和异常信息
     *
     * @param project 项目对象，表示异常发生的项目
     * @param message 异常信息描述
     * @since 0.0.1
     */
    public UploadException(Project project, String message) {
        super(message);
        this.project = project;
    }

    /**
     * 上传异常类的构造函数
     * <p>
     * 使用指定的消息和原因初始化上传异常
     *
     * @param message 异常消息
     * @param cause   导致异常的根本原因
     * @since 0.0.1
     */
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 初始化一个 UploadException 异常对象。
     * <p>
     * 该构造函数用于创建 UploadException 异常，并将指定的 Throwable 对象作为原因传递给父类构造函数。
     *
     * @param cause 异常的原因，通常为其他异常对象
     * @since 0.0.1
     */
    public UploadException(Throwable cause) {
        super(cause);
    }

    /**
     * 上传异常的构造函数
     * <p>
     * 初始化一个上传异常对象，设置异常信息、原因以及是否启用抑制和是否可写堆栈跟踪
     *
     * @param message            异常信息
     * @param cause              异常原因
     * @param enableSuppression  是否启用抑制
     * @param writableStackTrace 是否可写堆栈跟踪
     * @since 0.0.1
     */
    protected UploadException(String message,
                              Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 获取上传异常对象
     * <p>
     * 返回当前实例作为上传异常对象
     *
     * @return 上传异常对象
     */
    @Override
    public UploadException get() {
        return this;
    }
}
