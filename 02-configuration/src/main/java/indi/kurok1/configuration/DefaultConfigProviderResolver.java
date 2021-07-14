package indi.kurok1.configuration;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ConfigProviderResolver} default implements
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class DefaultConfigProviderResolver extends ConfigProviderResolver {

    private final Map<ClassLoader, Config> registeredConfigs = new ConcurrentHashMap<>();

    @Override
    public Config getConfig() {
        return getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        return this.registeredConfigs.get(loader);
    }

    @Override
    public ConfigBuilder getBuilder() {
        return new DefaultConfigBuilder(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        registeredConfigs.put(classLoader, config);
    }

    @Override
    public void releaseConfig(Config config) {
        if (config == null)
            return;
        //look up
        Iterator<Map.Entry<ClassLoader, Config>> iterator = registeredConfigs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ClassLoader, Config> entry = iterator.next();
            if (config.equals(entry.getValue())) {
                iterator.remove();
                break;
            }
        }
    }
}
