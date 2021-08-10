package org.geektimes.configuration.microprofile.config.discover;

import java.net.URL;

/**
 * 基于网络协议的 {@link ConfigSourceFactory}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.10
 * @see org.geektimes.configuration.microprofile.config.discover.ConfigSourceFactory
 */
public abstract class ProtocolBasedConfigSourceFactory implements ConfigSourceFactory {


    public ProtocolBasedConfigSourceFactory() {
        String protocol = getSupportedProtocol();
        if (protocol == null || protocol.isEmpty())
            throw new NullPointerException();
    }

    public abstract String getSupportedProtocol();

    @Override
    public boolean isSupport(String name, URL resource) {
        return resource.getProtocol().equals(getSupportedProtocol());
    }
}
