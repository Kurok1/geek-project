package indi.kurok1.configuration.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class StringArrayConverter implements OrderedConverter<String[]> {

    private final String delimiter;

    private final int order;

    public StringArrayConverter() {
        this(",");
    }

    public StringArrayConverter(String delimiter) {
        this(delimiter, DEFAULT_ORDERED);
    }

    public StringArrayConverter(String delimiter, int order) {
        this.delimiter = delimiter;
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String[] convert(String value) throws IllegalArgumentException, NullPointerException {
        StringTokenizer tokenizer = new StringTokenizer(value, delimiter);
        List<String> tokens = new ArrayList<>();
        int size = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!delimiter.equals(token)) {
                size++;
                tokens.add(token);
            }

        }
        return tokens.toArray(new String[size]);
    }

}
