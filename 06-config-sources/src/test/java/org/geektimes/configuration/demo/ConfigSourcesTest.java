package org.geektimes.configuration.demo;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.geektimes.configuration.microprofile.config.AnnotationConfigBuilder;
import org.geektimes.configuration.microprofile.config.DefaultConfigProviderResolver;
import org.geektimes.configuration.microprofile.config.annotation.ConfigSource;
import org.geektimes.configuration.microprofile.config.annotation.ConfigSources;

/**
 * {@link org.geektimes.configuration.microprofile.config.annotation.ConfigSources} test
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.10
 */
@ConfigSources(
        value = {
                @ConfigSource(
                        resource = "fs://F:/github/geek-project/06-config-sources/src/main/resources/app.properties",
                        ordinal = 10000
                ),
                @ConfigSource(
                        resource = "json://F:/github/geek-project/06-config-sources//src/main/resources/app.json",
                        ordinal = 10010
                )
        }
)
public class ConfigSourcesTest {

    public static void main(String[] args) {
        DefaultConfigProviderResolver resolver = new DefaultConfigProviderResolver();
        resolver.configureConfigBuilder(new AnnotationConfigBuilder(ConfigSourcesTest.class, Thread.currentThread().getContextClassLoader()));
        ConfigProviderResolver.setInstance(resolver);
        Config config = ConfigProvider.getConfig();
        Integer age = config.getValue("kurok1.age", Integer.class);
        String name = config.getValue("key2.name1", String.class);
        System.out.println("load age from config : " + age);
        System.out.println("load name from config : " + name);
    }

}
