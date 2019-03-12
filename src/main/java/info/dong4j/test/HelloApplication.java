package info.dong4j.test;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.BaseComponent;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-11 21:24
 */
public class HelloApplication implements BaseComponent, Disposable {
    @Override
    public void dispose() {
        System.out.println("dispose");
    }

    @Override
    public void initComponent() {
        System.out.println("HelloApplication --> initComponent");
    }

    @Override
    public void disposeComponent() {
        System.out.println("HelloApplication --> disposeComponent");
    }

    @NotNull
    @Override
    public String getComponentName() {
        System.out.println("HelloApplication --> disposeComponent");
        return "HelloApplication";
    }
}
