package indi.kurok1.configuration.listener;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.14
 */
public class ConfigProviderInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Thread.currentThread().setContextClassLoader(sce.getServletContext().getClassLoader());
        ConfigProviderResolver resolver = ConfigProviderResolver.instance();
        Config config = resolver.getBuilder().forClassLoader(Thread.currentThread().getContextClassLoader())
                .addDefaultSources().addDiscoveredSources().addDiscoveredConverters().build();
        resolver.registerConfig(config, Thread.currentThread().getContextClassLoader());
        ConfigProviderResolver.setInstance(resolver);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ConfigProviderResolver resolver = ConfigProviderResolver.instance();
        Config config = resolver.getConfig(context.getClassLoader());
        resolver.releaseConfig(config);
    }
}
