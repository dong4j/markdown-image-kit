package info.dong4j.test;

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
public class HelloProject implements BaseComponent {
    @Override
    public void initComponent() {
        log.trace("HelloProject --> initComponent");
    }

    @Override
    public void disposeComponent() {
        log.trace("HelloProject --> disposeComponent");
    }

    @NotNull
    @Override
    public String getComponentName() {
        log.trace("HelloProject --> disposeComponent");
        return "HelloProject";
    }
}
