package org.geektimes.configuration.microprofile.config.discover;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.geektimes.configuration.microprofile.config.source.MapConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.eclipse.microprofile.config.spi.ConfigSource.DEFAULT_ORDINAL;

/**
 * 将一个资源作为{@link Properties}读取
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 * @see ConfigSourceLoader
 */
public abstract class PropertiesConfigSourceLoader implements ConfigSourceLoader {

    @Override
    public ConfigSource load(String name, Integer ordinal, URL url) {
        Properties properties = new Properties();
        try (InputStream inputStream = url.openStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int order = (ordinal == null || ordinal < 0) ? DEFAULT_ORDINAL : ordinal;
        if (name == null ||  name.isEmpty())
            name = generateConfigSourceName(url);
        return new MapConfigSource(name, order, properties);
    }


    /**
     * 自生成配置源名称
     * @param url url
     * @return 配置源名称
     */
    protected String generateConfigSourceName(URL url) {
        return String.format("%s@%d", url.toString(), System.identityHashCode(url));
    }
}
