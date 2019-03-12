package info.dong4j.test;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.util.InvalidDataException;

import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-11 21:35
 */
public class HelloModule extends ProjectManager implements ModuleComponent {
    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void moduleAdded() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "HelloModule";
    }

    @Override
    public void addProjectManagerListener(@NotNull ProjectManagerListener listener) {

    }

    @Override
    public void addProjectManagerListener(@NotNull VetoableProjectManagerListener listener) {

    }

    @Override
    public void addProjectManagerListener(@NotNull ProjectManagerListener listener, @NotNull Disposable parentDisposable) {

    }

    @Override
    public void removeProjectManagerListener(@NotNull ProjectManagerListener listener) {

    }

    @Override
    public void removeProjectManagerListener(@NotNull VetoableProjectManagerListener listener) {

    }

    @Override
    public void addProjectManagerListener(@NotNull Project project, @NotNull ProjectManagerListener listener) {

    }

    @Override
    public void removeProjectManagerListener(@NotNull Project project, @NotNull ProjectManagerListener listener) {

    }

    @NotNull
    @Override
    public Project[] getOpenProjects() {
        return new Project[0];
    }

    @NotNull
    @Override
    public Project getDefaultProject() {
        return null;
    }

    @Nullable
    @Override
    public Project loadAndOpenProject(@NotNull String filePath) throws IOException, JDOMException, InvalidDataException {
        return null;
    }

    @Override
    public boolean closeProject(@NotNull Project project) {
        return false;
    }

    @Override
    public void reloadProject(@NotNull Project project) {

    }

    @Nullable
    @Override
    public Project createProject(@Nullable String name, @NotNull String path) {
        return null;
    }
}
