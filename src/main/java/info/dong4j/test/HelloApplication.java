package info.dong4j.test;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.BaseComponent;

import org.jetbrains.annotations.NotNull;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-11 21:24
 */
@Slf4j
public class HelloApplication implements BaseComponent, Disposable {
    @Override
    public void dispose() {
        log.debug("dispose");
    }

    @Override
    public void initComponent() {
        log.debug("HelloApplication --> initComponent");
    }

    @Override
    public void disposeComponent() {
        log.debug("HelloApplication --> disposeComponent");
    }

    @NotNull
    @Override
    public String getComponentName() {
        log.debug("HelloApplication --> disposeComponent");
        return "HelloApplication";
    }
}
