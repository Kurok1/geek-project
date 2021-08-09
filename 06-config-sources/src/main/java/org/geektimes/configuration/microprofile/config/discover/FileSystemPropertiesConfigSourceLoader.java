package org.geektimes.configuration.microprofile.config.discover;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 从本地文件系统中，加载{@link org.eclipse.microprofile.config.spi.ConfigSource}
 * 要求{@link URL}满足如下条件
 * <ul>
 *     <li>协议必须以满足{@link FileSystemPropertiesConfigSourceLoader#SUPPORT_PROTOCOL}</li>
 *     <li>文件必须在本地文件系统中存在且可读</li>
 * </ul>
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 * @see PropertiesConfigSourceLoader
 */
public class FileSystemPropertiesConfigSourceLoader extends PropertiesConfigSourceLoader {

    public final static List<String> SUPPORT_PROTOCOL = Collections.unmodifiableList(Arrays.asList("fs", "FS"));

    @Override
    public boolean isSupport(URL url) {
        //检查协议
        String protocol = url.getProtocol();
        if (!SUPPORT_PROTOCOL.contains(protocol))
            return false;

        try (InputStream inputStream = url.openStream()) {
            return inputStream.available() > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
