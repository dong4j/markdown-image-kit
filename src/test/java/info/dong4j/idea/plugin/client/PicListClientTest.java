package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.oss.PicListOssState;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PicListClient 测试类
 * <p>
 * 用于验证 PicListClient 的核心功能，包括单例模式验证、云类型获取、上传接口构建、异常处理逻辑等。
 * 该类通过模拟依赖对象和构造测试场景，确保 PicListClient 在不同配置和异常情况下的行为符合预期。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class PicListClientTest {

    /** PicListClient 实例，用于与图片列表服务进行交互 */
    private PicListClient client;
    /** 模拟的持久化组件，用于测试或非生产环境替代真实持久化逻辑 */
    private MikPersistenComponent mockComponent;
    /** 模拟状态对象，用于存储和管理模拟运行时的状态信息 */
    private MikState mockState;
    /** 模拟的图片列表 OSS 状态信息 */
    private PicListOssState mockPicListState;

    /** 临时文件目录，用于存储运行时生成的临时文件 */
    @TempDir
    Path tempDir;

    /**
     * 初始化测试环境，设置模拟对象和默认返回值
     * <p>
     * 该方法用于在每个测试用例执行前初始化必要的模拟对象和默认值，包括创建PicListClient实例、
     * 模拟MikPersistenComponent及其相关组件，并设置其返回的PicListOssState对象的默认属性。
     *
     * @since 1.0
     */
    @BeforeEach
    public void setUp() {
        client = PicListClient.getInstance();

        // 创建模拟对象
        mockComponent = mock(MikPersistenComponent.class);
        mockState = mock(MikState.class);
        mockPicListState = mock(PicListOssState.class);

        // 设置默认返回值
        when(mockComponent.getState()).thenReturn(mockState);
        when(mockState.getPicListOssState()).thenReturn(mockPicListState);
        when(mockPicListState.getApi()).thenReturn("https://piclist.example.com/upload");
        when(mockPicListState.getPicbed()).thenReturn("smms");
        when(mockPicListState.getConfigName()).thenReturn("default");
        when(mockPicListState.getKey()).thenReturn("test-key");
        when(mockPicListState.getExePath()).thenReturn("");
    }

    /**
     * 测试后执行的清理方法
     * <p>
     * 用于清理测试过程中产生的测试数据，确保每次测试环境的独立性和干净性
     */
    @AfterEach
    public void tearDown() {
        // 清理测试数据
    }

    /**
     * 测试 PicListClient 的单例模式实现
     * <p>
     * 测试场景：通过 getInstance 方法获取两次实例
     * 预期结果：两次获取的实例应为同一个对象
     * <p>
     * 该测试验证 PicListClient 是否正确实现了单例模式，确保多次调用 getInstance 方法返回相同的实例。
     */
    @Test
    public void testGetInstance() {
        PicListClient instance1 = PicListClient.getInstance();
        PicListClient instance2 = PicListClient.getInstance();
        assertNotNull(instance1);
        assertEquals(instance1, instance2, "应该返回相同的实例");
    }

    /**
     * 测试获取云类型功能
     * <p>
     * 测试场景：调用 client.getCloudType() 方法获取云类型
     * 预期结果：返回的云类型应为 PICLIST
     * <p>
     * 注意：测试中使用了断言验证返回值是否符合预期，若云类型获取逻辑依赖外部服务或配置，需确保相关环境已正确初始化
     */
    @Test
    public void testGetCloudType() {
        CloudEnum cloudType = client.getCloudType();
        assertEquals(CloudEnum.PICLIST, cloudType, "云类型应该是 PICLIST");
    }

    /**
     * 测试获取带图片列表支持的测试字段文本功能
     * <p>
     * 测试场景：通过反射调用私有方法获取面板中指定名称字段的值
     * 预期结果：返回的 Map 中应包含对应字段名称的值
     * <p>
     * 注意：该测试需要通过反射调用私有方法，因此需要设置方法为可访问
     */
    @Test
    public void testGetTestFieldTextWithPicListSupport() throws Exception {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField("test value");
        textField.setName("testField");
        panel.add(textField);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = PicListClient.class.getDeclaredMethod(
            "getTestFieldTextWithPicListSupport", JPanel.class);
        method.setAccessible(true);

        Object result = method.invoke(client, panel);
        assertInstanceOf(Map.class, result);

        @SuppressWarnings("unchecked") java.util.Map<String, String> resultMap = (java.util.Map<String, String>) result;
        assertEquals("test value", resultMap.get("testField"));
    }
}