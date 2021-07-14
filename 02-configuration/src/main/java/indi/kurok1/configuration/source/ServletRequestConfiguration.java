package indi.kurok1.configuration.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.servlet.ServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class ServletRequestConfiguration implements ConfigSource {

    private final ServletRequest servletRequest;

    private final String name;

    public ServletRequestConfiguration(String name, ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
        this.name = name;
    }

    @Override
    public Set<String> getPropertyNames() {
        return servletRequest.getParameterMap().keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return servletRequest.getParameter(propertyName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getProperties() {
        return ConfigSource.super.getProperties();
    }

    @Override
    public int getOrdinal() {
        return ConfigSource.super.getOrdinal();
    }
}
