package indi.kurok1.pre.url;

import indi.kurok1.pre.url.impl.CompositeURLPatternsMatcher;

import java.util.Arrays;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.03
 */
public class TestApplication {

    public static void main(String[] args) {

        List<String> urlPatterns = Arrays.asList("/**/aa", "/**/aa/b.*", "/aa/**/BB", "/aa/xx/BB?1", "/aa/xx/BB*1", "/hh/**/a*.html");
        String url = "/hh/BB2/aa/b.jsp";


        URLPatternsMatcher matcher = new CompositeURLPatternsMatcher();
        System.out.println(matcher.matches(urlPatterns, url));
    }

}
