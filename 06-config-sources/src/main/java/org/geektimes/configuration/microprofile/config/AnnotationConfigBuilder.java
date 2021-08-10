package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.geektimes.configuration.microprofile.config.discover.ConfigSourcesScanner;

/**
 * 指定根{@link Class},完成{@link org.geektimes.configuration.microprofile.config.annotation.ConfigSource}和{@link org.geektimes.configuration.microprofile.config.annotation.ConfigSources}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.10
 */
public class AnnotationConfigBuilder extends DefaultConfigBuilder {

    private final String basePackage;
    private final Class<?> source;
    private final ConfigSourcesScanner scanner;

    public AnnotationConfigBuilder(Class<?> sourceCLass, ClassLoader classLoader) {
        super(classLoader);
        this.source = sourceCLass;
        this.basePackage = resolveSourceClassPackage(sourceCLass);
        this.scanner = new ConfigSourcesScanner(getConfigSources(), classLoader);
    }

    private static String resolveSourceClassPackage(Class<?> sourceClass) {
        return sourceClass.getPackage().getName();
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        super.addDiscoveredSources();
        this.scanner.scan(this.basePackage);
        return this;
    }
}
