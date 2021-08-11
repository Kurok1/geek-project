package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultConfigProviderResolver extends ConfigProviderResolver {

    private ConcurrentMap<ClassLoader, Config> configsRepository = new ConcurrentHashMap<>();

    private ConfigBuilder configBuilder = null;

    @Override
    public Config getConfig() {
        return getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        return configsRepository.computeIfAbsent(loader, this::newConfig);
    }

    private ClassLoader resolveClassLoader(ClassLoader classLoader) {
        return classLoader == null ? this.getClass().getClassLoader() : classLoader;
    }

   private ConfigBuilder loadConfigBuilder(ClassLoader classLoader) {
       ServiceLoader<ConfigBuilder> serviceLoader = ServiceLoader.load(ConfigBuilder.class, classLoader);
       Iterator<ConfigBuilder> iterator = serviceLoader.iterator();
       if (iterator.hasNext()) {
           // 获取 Config SPI 第一个实现
           return iterator.next();
       }
       return new DefaultConfigBuilder(classLoader);
       //throw new IllegalStateException("No Config implementation found!");
   }

   public void configureConfigBuilder(ConfigBuilder configBuilder) {
        this.configBuilder = configBuilder;
   }

    @Override
    public ConfigBuilder getBuilder() {
        return configBuilder == null ? newConfigBuilder(null) : configBuilder;
    }

    protected ConfigBuilder newConfigBuilder(ClassLoader classLoader) {
        return configBuilder == null ? loadConfigBuilder(resolveClassLoader(classLoader)) : configBuilder;
    }

    protected Config newConfig(ClassLoader classLoader) {
        return newConfigBuilder(classLoader)
                .addDefaultSources()
                .addDiscoveredSources()
                .addDiscoveredConverters()
                .build();
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        configsRepository.put(classLoader, config);
    }

    @Override
    public void releaseConfig(Config config) {
        List<ClassLoader> targetKeys = new LinkedList<>();
        for (Map.Entry<ClassLoader, Config> entry : configsRepository.entrySet()) {
            if (Objects.equals(config, entry.getValue())) {
                targetKeys.add(entry.getKey());
            }
        }
        targetKeys.forEach(configsRepository::remove);
    }
}
