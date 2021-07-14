package indi.kurok1.configuration;

import indi.kurok1.configuration.source.ServletRequestConfiguration;
import org.eclipse.microprofile.config.spi.ConfigBuilder;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public final class ServletRequestConfigBuilder extends DefaultConfigBuilder {

    public ServletRequestConfigBuilder(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        ServletRequestWrapper wrapper = new ServletRequestWrapper();
        return super.withSources(new ServletRequestConfiguration("my-servlet", wrapper));
    }
}
