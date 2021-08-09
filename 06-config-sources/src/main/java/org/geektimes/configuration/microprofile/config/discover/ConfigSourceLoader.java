package org.geektimes.configuration.microprofile.config.discover;


import org.eclipse.microprofile.config.spi.ConfigSource;

import java.net.URL;

/**
 * 从{@link URL}中装载{@link ConfigSource}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 * @see URL
 * @see ConfigSource
 */
public interface ConfigSourceLoader {

    /**
     * 判断当前url是否支持装载
     * @param url url
     * @return 是否支持读取
     */
    boolean isSupport(URL url);

    /**
     * 从url中读取配置源，并返回
     * @param name 配置源名称
     * @param ordinal ordinal
     * @param url url资源位置
     * @return 配置源 non-null
     */
    ConfigSource load(String name, Integer ordinal, URL url);

}
