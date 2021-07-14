package indi.kurok1.configuration.converter;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class BigDecimalConverter implements OrderedConverter<BigDecimal> {

    public final int order;

    public BigDecimalConverter() {
        this(DEFAULT_ORDERED);
    }

    public BigDecimalConverter(int order) {
        if (order < 0)
            throw new IllegalArgumentException();
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public BigDecimal convert(String value) throws IllegalArgumentException, NullPointerException {
        if (value == null || value.isEmpty())
            throw new NullPointerException();
        return new BigDecimal(value);
    }
}
