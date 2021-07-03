package indi.kurok1.pre.url;

import java.util.Collection;

/**
 * URL路径匹配器
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.03
 */
public interface URLPatternsMatcher {

    boolean matches(Collection<String> urlPatterns, String requestURI);

}
