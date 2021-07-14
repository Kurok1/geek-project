package indi.kurok1.configuration.converter;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class IntegerConverter implements OrderedConverter<Integer> {

    public final int order;

    public IntegerConverter() {
        this(DEFAULT_ORDERED);
    }

    public IntegerConverter(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Integer convert(String value) throws IllegalArgumentException, NullPointerException {
        if (value == null || value.isEmpty())
            throw new NullPointerException();
        return Integer.decode(value);
    }
}
