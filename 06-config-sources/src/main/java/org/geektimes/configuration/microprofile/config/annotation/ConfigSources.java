package org.geektimes.configuration.microprofile.config.annotation;

import java.lang.annotation.*;

/**
 * Repeatable of {@link ConfigSource}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.09
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigSources {

    ConfigSource[] value();

}
