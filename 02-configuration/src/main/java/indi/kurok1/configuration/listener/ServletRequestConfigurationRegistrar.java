package indi.kurok1.configuration.listener;

import indi.kurok1.configuration.source.ConfigSources;
import indi.kurok1.configuration.source.ServletRequestConfiguration;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import java.util.UUID;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.14
 */
public class ServletRequestConfigurationRegistrar implements ServletRequestListener {

    public final String CONFIG_NAME = "indi.kurok1.configuration.servlet.name";

    private ConfigProviderResolver getConfigProviderResolver() {
        return ConfigProviderResolver.instance();
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        //解除绑定
        ConfigProviderResolver resolver = getConfigProviderResolver();
        ServletRequest request = sre.getServletRequest();
        ServletContext context = sre.getServletContext();
        String configName = (String) request.getAttribute(CONFIG_NAME);
        if (configName != null) {
            //存在配置，取消绑定ConfigSource
            Config config = resolver.getConfig(context.getClassLoader());
            ConfigSources configSources = (ConfigSources) config.getConfigSources();
            configSources.release(configName);
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ConfigProviderResolver resolver = getConfigProviderResolver();
        ServletRequest request = sre.getServletRequest();
        ServletContext context = sre.getServletContext();

        Config config = resolver.getConfig(context.getClassLoader());
        Iterable<ConfigSource> configSources = config.getConfigSources();
        if (configSources instanceof ConfigSources) {
            //可配置的
            String configName = generateConfigName();
            ServletRequestConfiguration configuration = new ServletRequestConfiguration(configName, request);
            ((ConfigSources) configSources).addLast(configuration);
            request.setAttribute(CONFIG_NAME, generateConfigName());
        }
    }

    private String generateConfigName() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return String.format("servlet-%s", uuid);
    }
}
