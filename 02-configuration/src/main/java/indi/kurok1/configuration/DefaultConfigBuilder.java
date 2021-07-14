package indi.kurok1.configuration;

import indi.kurok1.configuration.converter.Converters;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class DefaultConfigBuilder implements ConfigBuilder {

    private final List<ConfigSource> configSources = new CopyOnWriteArrayList<>();

    private final Converters converters = new Converters();

    private ClassLoader classLoader;

    public DefaultConfigBuilder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        //todo...
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        //todo...
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        Iterable<Converter> converters = ServiceLoader.load(Converter.class, this.classLoader);
        Iterator<Converter> iterator = converters.iterator();
        while (iterator.hasNext()) {
            withConverters(iterator.next());
        }
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        this.classLoader = loader;
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        configSources.addAll(Arrays.asList(sources));
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        if (converters == null)
            throw new NullPointerException();
        for (Converter converter : converters)
            this.converters.register(converter);
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        this.converters.register(type, converter, priority);
        return this;
    }

    @Override
    public Config build() {
        Config config = new DefaultConfig(configSources, converters);
        return config;
    }
}
