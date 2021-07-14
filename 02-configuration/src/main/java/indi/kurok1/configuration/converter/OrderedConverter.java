package indi.kurok1.configuration.converter;

import org.eclipse.microprofile.config.spi.Converter;

/**
 * Convert with order
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public interface OrderedConverter<T> extends Converter<T> {

    int getOrder();

    int MAX_ORDERED = 0;
    int MIN_ORDERED = Integer.MIN_VALUE;
    int DEFAULT_ORDERED = 100;

}
