/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geektimes.configuration.microprofile.config.discover.classpath;

import org.apache.commons.lang.StringUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.geektimes.configuration.microprofile.config.discover.ConfigSourceFactory;
import org.geektimes.configuration.microprofile.config.discover.ProtocolBasedConfigSourceFactory;
import org.geektimes.configuration.microprofile.config.source.MapConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/**
 * The implementation of  {@link ConfigSourceFactory}
 * load from properties file
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ClassPathPropertiesConfigSourceFactory extends ProtocolBasedConfigSourceFactory {

    @Override
    public ConfigSource createConfigSource(String name, int ordinal, URL resource, String encoding) {
        ConfigSource configSource = null;
        try (InputStream inputStream = resource.openStream();
             InputStreamReader reader = new InputStreamReader(inputStream, encoding)) {
            Properties properties = new Properties();
            properties.load(reader);
            String actualName = StringUtils.isBlank(name) ? resource.toString() : name;
            configSource = new MapConfigSource(actualName, ordinal, properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configSource;
    }

    @Override
    public boolean isSupport(String name, URL resource) {
        //检查协议
        boolean checkProtocol = super.isSupport(name, resource);
        if (!checkProtocol)
            return false;
        //文件名以.properties结尾
        String realPath = "";
        if (resource.getPath() == null || resource.getPath().isEmpty())
            realPath = "/" + resource.getAuthority();
        else realPath = String.format("%s%s", resource.getAuthority(), resource.getPath());
        return realPath.endsWith(".properties");
    }

    @Override
    public String getSupportedProtocol() {
        return "classpath";
    }
}
