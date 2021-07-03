package indi.kurok1.pre.url;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.03
 */
public class StringUtils {


    public static String[] splitString(String source, String delimiter) {
        StringTokenizer tokenizer = new StringTokenizer(source, delimiter);
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
