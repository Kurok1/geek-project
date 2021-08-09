package org.geektimes.configuration.demo;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.geektimes.configuration.microprofile.config.annotation.ConfigSource;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.10
 */
@ConfigSource(
        name = "test",
        resource = "fs://F:/learning/geek-project/06-config-sources/src/main/resources/app.properties",
        ordinal = 10000
)
public class ConfigSourcesTest {

    public static void main(String[] args) {
        Config config = ConfigProvider.getConfig();
        Integer age = config.getValue("kurok1.age", Integer.class);
        System.out.println(age);
    }

}
