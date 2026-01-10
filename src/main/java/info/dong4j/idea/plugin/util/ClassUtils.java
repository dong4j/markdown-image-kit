package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 类工具类
 * <p>
 * 提供与 Java 类相关的实用方法，包括根据包名获取包内所有类、根据接口获取实现该接口的所有类、获取包内所有类名以及获取包内所有类等操作。该类主要用于类路径扫描和类加载相关场景。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.23
 * @since 0.0.1
 */
@SuppressWarnings("D")
@Slf4j
public final class ClassUtils {
    /**
     * 通过包名获取包内所有类
     * <p>
     * 该方法根据指定的包对象获取其名称，并查找该包及其子包下的所有类。
     *
     * @param pkg 包对象，用于获取包名
     * @return 包内所有类的名称列表
     * @since 0.0.1
     */
    public static List<String> getAllClassByPackageName(@NotNull Package pkg) {
        String packageName = pkg.getName();
        // 获取当前包下以及子包下所以的类
        return getClasses(packageName, null);
    }

    /**
     * 根据接口类获取该接口下所有实现该接口的类名称列表
     * <p>
     * 该方法会检查传入的类是否为接口，如果是则查找当前包及其子包下所有实现该接口的类，并过滤掉内部类。
     *
     * @param c 接口类对象
     * @return 实现该接口的类名称列表，若传入的类不是接口则返回空列表
     */
    static List<String> getAllClassByInterface(@NotNull Class<?> c) {
        List<String> returnClassList = new ArrayList<>();
        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所有的类
            returnClassList = getClasses(packageName, c);

            // 排除内部类
            returnClassList = returnClassList.stream().filter(s -> !s.contains("$")).collect(Collectors.toList());
        }
        return returnClassList;
    }

    /**
     * 获取某一类所在包下的所有类名（不包含迭代器类）
     * <p>
     * 根据给定的类路径和包名，查找该包下所有的类名。若目录存在，则返回类名数组；否则返回空数组。
     *
     * @param classLocation 类路径，用于构建完整的文件路径
     * @param packageName   包名，用于确定要查找的包路径
     * @return 包含该包下所有类名的字符串数组，若目录不存在则返回空数组
     * @since 0.0.1
     */
    @Nullable
    public static String[] getPackageAllClassName(String classLocation, @NotNull String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        StringBuilder realClassLocation = new StringBuilder(classLocation);
        for (String s : packagePathSplit) {
            realClassLocation.append(File.separator).append(s);
        }
        File packeageDir = new File(realClassLocation.toString());
        if (packeageDir.isDirectory()) {
            return packeageDir.list();
        }
        return null;
    }

    /**
     * 从指定的包路径中获取所有类的全限定名
     * <p>
     * 该方法会扫描指定包路径下的所有类文件，包括jar包中的类文件，并将它们的全限定名收集到列表中。
     *
     * @param packageName 要扫描的包名
     * @param c           用于获取类加载器的类对象，通常用于确定扫描范围
     * @return 包含所有类全限定名的列表
     * @since 0.0.1
     */
    private static List<String> getClasses(@NotNull String packageName, Class<?> c) {
        // 第一个class类的集合
        List<String> classes = new ArrayList<>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = c.getClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                // 如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !entry.isDirectory() && !name.contains("&")) {
                                    // 去掉后面的".class" 获取真正的类名
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    // 添加到classes
                                    classes.add(packageName + '.' + className);
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.debug("", e);
                    }
                }
            }
        } catch (IOException e) {
            log.debug("", e);
        }

        return classes;
    }

    /**
     * 以文件形式获取指定包下的所有类名
     * <p>
     * 遍历指定包路径下的所有文件，递归查找所有Java类文件，并将类名添加到指定列表中。
     *
     * @param packageName 包名，用于构建完整的类名
     * @param packagePath 包对应的文件系统路径
     * @param recursive   是否递归查找子目录
     * @param classes     用于存储找到的类名的列表
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, boolean recursive, List<String> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        // 循环所有文件
        assert dirfiles != null;
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                // 添加到集合中去
                classes.add(packageName + '.' + className);
            }
        }
    }
}
