package org.geektimes.configuration.demo;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.geektimes.configuration.microprofile.config.AnnotationConfigBuilder;
import org.geektimes.configuration.microprofile.config.DefaultConfigProviderResolver;
import org.geektimes.configuration.microprofile.config.annotation.ConfigSource;

/**
 * {@link org.geektimes.configuration.microprofile.config.annotation.ConfigSources} test
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.10
 */
@ConfigSource(
        resource = "fs://F:/learning/geek-project/06-config-sources/src/main/resources/app.properties",
        ordinal = 10000
)
@ConfigSource(
        resource = "json://F:/learning/geek-project/06-config-sources//src/main/resources/app.json",
        ordinal = 10010
)
public class ConfigSourcesTest {

    public static void main(String[] args) {
        DefaultConfigProviderResolver resolver = new DefaultConfigProviderResolver();
        resolver.configureConfigBuilder(new AnnotationConfigBuilder(ClassPathTest.class, Thread.currentThread().getContextClassLoader()));
        ConfigProviderResolver.setInstance(resolver);
        Config config = ConfigProvider.getConfig();
        Integer age = config.getValue("kurok1.age", Integer.class);
        System.out.println(age);
    }

}
