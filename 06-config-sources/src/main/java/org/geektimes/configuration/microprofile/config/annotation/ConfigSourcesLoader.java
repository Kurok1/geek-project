package org.geektimes.configuration.microprofile.config.annotation;

import org.geektimes.configuration.microprofile.config.discover.ConfigSourceLoader;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 组合加载 {@link ConfigSource}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 */
public class ConfigSourcesLoader implements ConfigSourceLoader {

    private final List<ConfigSourceLoader> loaders = new LinkedList<>();

    private final ClassLoader classLoader;


    public ConfigSourcesLoader() {
        this(ServiceLoader.load(ConfigSourceLoader.class), Thread.currentThread().getContextClassLoader());
    }

    public ConfigSourcesLoader(ClassLoader classLoader) {
        this(ServiceLoader.load(ConfigSourceLoader.class), classLoader);
    }

    public ConfigSourcesLoader(Iterable<ConfigSourceLoader> loaders, ClassLoader classLoader) {
        initLoaders(loaders);
        this.classLoader = classLoader;
    }

    private void initLoaders(Iterable<ConfigSourceLoader> loaders) {
        for (ConfigSourceLoader loader : loaders) {
            this.loaders.add(loader);
        }
    }

    @Override
    public boolean isSupport(URL url) {
        if (loaders.isEmpty())
            throw new IllegalStateException("no config source load found");

        for (ConfigSourceLoader loader : loaders) {
            if (loader.isSupport(url))
                return true;
        }

        return false;
    }

    /**
     * 根据url找到合适的loader
     * @param url
     * @return 没有合适的返回null
     */
    public ConfigSourceLoader getSuitableConfigSourceLoader(URL url) {
        if (loaders.isEmpty())
            throw new IllegalStateException("no config source load found");

        for (ConfigSourceLoader loader : loaders) {
            if (loader.isSupport(url))
                return loader;
        }

        return null;
    }

    @Override
    public org.eclipse.microprofile.config.spi.ConfigSource load(String name, Integer ordinal, URL url) {
        ConfigSourceLoader loader = getSuitableConfigSourceLoader(url);
        return loader.load(name, ordinal, url);
    }
}
