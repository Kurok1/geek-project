package org.geektimes.configuration.microprofile.config.annotation;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * 指定扫描包，扫描包下的所有类，检查是否标记{@link ConfigSource} 或者 {@link ConfigSources}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 */
public class ConfigSourcesScanner {

    private final static List<Class<? extends Annotation>> SUPPORTED_ANNOTATION_TYPES = Collections.unmodifiableList(Arrays.asList(
            ConfigSource.class, ConfigSources.class
    ));

    private ConfigSource[] resolveConfigSources(ConfigSources configSources) {
        if (configSources != null) {
            return configSources.value();
        }
        return null;
    }

    private final org.geektimes.configuration.microprofile.config.source.ConfigSources configSources;
    private final ConfigSourcesLoader loader;
    private final ClassLoader classLoader;

    public ConfigSourcesScanner(org.geektimes.configuration.microprofile.config.source.ConfigSources configSources, ClassLoader classLoader) {
        this.configSources = configSources;
        this.classLoader = classLoader;
        this.loader = new ConfigSourcesLoader(classLoader);
    }

    public void scan(String basePackageName) {
        basePackageName = basePackageName.replace(".", "/");
        scanInternal(basePackageName, this.classLoader);
    }

    protected void scanInternal(String basePath, ClassLoader classLoader) {
        try {
            Enumeration<URL> resources = classLoader.getResources(basePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println(resource.getFile());
                File file = new File(resource.getFile());
                if (file.exists() && file.isDirectory()) {
                    String[] fileList = file.list();
                    for (String fileName : fileList) {
                        if (fileName.endsWith(".class")) {
                            fileName = fileName.substring(0, fileName.length() - 6);
                            fileName = basePath + "/" + fileName;
                            resolveClass(fileName.replace("/", "."));
                            continue;
                        }
                        //目录文件递归调用
                        scanInternal(basePath + "/" + fileName, classLoader);
                    }
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析一个类，并且加载配置
     * @param className 类的全限定名称
     */
    protected void resolveClass(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className, false, this.classLoader);
        List<ConfigSource> resolvedConfigSourceList = new LinkedList<>();
        if (clazz.isAnnotationPresent(ConfigSources.class)) {
            ConfigSources configSources = clazz.getAnnotation(ConfigSources.class);
            ConfigSource[] configSourceList = resolveConfigSources(configSources);
            resolvedConfigSourceList.addAll(Arrays.asList(configSourceList));
        }
        if (clazz.isAnnotationPresent(ConfigSource.class)) {
            resolvedConfigSourceList.add(clazz.getAnnotation(ConfigSource.class));
        }
        if (resolvedConfigSourceList.isEmpty())
            return;

        loadConfigSources(resolvedConfigSourceList.toArray(new ConfigSource[0]));
    }

    private void loadConfigSources(ConfigSource[] configSources) {
        if (configSources == null || configSources.length == 0) {
            return;
        }

        for (ConfigSource configSource : configSources) {
            loadConfigSource(configSource);
        }
    }

    protected void loadConfigSource(ConfigSource configSource) {
        String name = configSource.name();
        String path = configSource.resource();
        int ordinal = configSource.ordinal();
        URL url = null;
        try {
            url = new URL(path);
            org.eclipse.microprofile.config.spi.ConfigSource source = this.loader.load(name, ordinal, url);
            this.configSources.addConfigSources(source);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
