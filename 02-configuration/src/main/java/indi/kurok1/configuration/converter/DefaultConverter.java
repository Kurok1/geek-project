package indi.kurok1.configuration.converter;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class DefaultConverter implements OrderedConverter<String> {

    @Override
    public int getOrder() {
        return DEFAULT_ORDERED;
    }

    @Override
    public String convert(String value) throws IllegalArgumentException, NullPointerException {
        return value;
    }
}
