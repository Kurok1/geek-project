package org.geektimes.configuration.microprofile.config.discover.fs;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.geektimes.configuration.microprofile.config.discover.ProtocolBasedConfigSourceFactory;
import org.geektimes.configuration.microprofile.config.source.MapConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * 从本地文件系统中，加载{@link org.eclipse.microprofile.config.spi.ConfigSource}
 * 要求{@link URL}满足如下条件
 * <ul>
 *     <li>协议必须以满足{@link FileSystemPropertiesConfigSourceFactory#SUPPORT_PROTOCOL}</li>
 *     <li>文件必须在本地文件系统中存在且可读</li>
 * </ul>
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 * @see ProtocolBasedConfigSourceFactory
 */
public class FileSystemPropertiesConfigSourceFactory extends ProtocolBasedConfigSourceFactory {

    public final static String SUPPORT_PROTOCOL = "fs";

    public FileSystemPropertiesConfigSourceFactory() {
        super();
    }

    @Override
    public String getSupportedProtocol() {
        return SUPPORT_PROTOCOL;
    }

    @Override
    public boolean isSupport(String name, URL url) {
        //检查协议
        boolean checkProtocol = super.isSupport(name, url);
        if (!checkProtocol)
            return false;

        try (InputStream inputStream = url.openStream()) {
            return inputStream.available() > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConfigSource createConfigSource(String name, int ordinal, URL resource, String encoding) {
        Properties properties = new Properties();
        try (InputStream inputStream = resource.openStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new MapConfigSource(name, ordinal, properties);
    }


}
