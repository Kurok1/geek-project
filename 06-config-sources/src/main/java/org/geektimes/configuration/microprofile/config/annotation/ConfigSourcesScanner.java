package org.geektimes.configuration.microprofile.config.annotation;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

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

    public ConfigSourcesScanner(org.geektimes.configuration.microprofile.config.source.ConfigSources configSources, ClassLoader classLoader) {
        this.configSources = configSources;
        this.loader = new ConfigSourcesLoader(classLoader);
    }

    public void scan(String basePackageName) {
        try {
            basePackageName = basePackageName.replace(".", "/");
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(basePackageName);
            while (resources.hasMoreElements()) {
                //todo 递归解析


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
