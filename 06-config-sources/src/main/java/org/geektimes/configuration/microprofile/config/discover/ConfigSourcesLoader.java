package org.geektimes.configuration.microprofile.config.discover;

import org.geektimes.configuration.microprofile.config.annotation.ConfigSource;

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
public class ConfigSourcesLoader implements ConfigSourceFactory {

    private final List<ConfigSourceFactory> loaders = new LinkedList<>();

    private final ClassLoader classLoader;


    public ConfigSourcesLoader() {
        this(ServiceLoader.load(ConfigSourceFactory.class), Thread.currentThread().getContextClassLoader());
    }

    public ConfigSourcesLoader(ClassLoader classLoader) {
        this(ServiceLoader.load(ConfigSourceFactory.class), classLoader);
    }

    public ConfigSourcesLoader(Iterable<ConfigSourceFactory> loaders, ClassLoader classLoader) {
        initLoaders(loaders);
        this.classLoader = classLoader;
    }

    private void initLoaders(Iterable<ConfigSourceFactory> loaders) {
        for (ConfigSourceFactory loader : loaders) {
            if (loader instanceof ConfigSourcesLoader)
                continue;
            this.loaders.add(loader);
        }
    }

    @Override
    public boolean isSupport(String name, URL url) {
        if (loaders.isEmpty())
            throw new IllegalStateException("no config source load found");

        for (ConfigSourceFactory loader : loaders) {
            if (loader.isSupport(name, url))
                return true;
        }

        return false;
    }

    /**
     * 根据url找到合适的loader
     * @param url
     * @return 没有合适的返回null
     */
    public ConfigSourceFactory getSuitableConfigSourceFactory(String name, URL url) {
        if (loaders.isEmpty())
            throw new IllegalStateException("no config source load found");

        for (ConfigSourceFactory loader : loaders) {
            if (loader.isSupport(name, url))
                return loader;
        }

        return null;
    }

    @Override
    public org.eclipse.microprofile.config.spi.ConfigSource createConfigSource(String name, int ordinal, URL resource, String encoding) {
        if (name == null || name.isEmpty())
            name = generateConfigSourceName(resource);
        ConfigSourceFactory factory = getSuitableConfigSourceFactory(name, resource);
        return factory.createConfigSource(name, ordinal, resource, encoding);
    }

    public ConfigSourceFactory getConfigSourceFactory(Class<? extends ConfigSourceFactory> clazz) {
        if (clazz != null) {
            return loaders.stream().filter(
                    loader->clazz.equals(loader.getClass())
            ).findAny().orElse(null);
        }

        return null;
    }
}
