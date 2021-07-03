package indi.kurok1.pre.url.impl;

import indi.kurok1.pre.url.URLPatternsMatcher;

import java.util.Collection;

/**
 * 路径必须完全相同
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.03
 */
public class EqualsURLPatternsMatcher implements URLPatternsMatcher {

    @Override
    public boolean matches(Collection<String> urlPatterns, String requestURI) {
        if (requestURI == null || requestURI.length() == 0)
            return false;
        for (String urlPattern : urlPatterns)
            if (requestURI.equals(urlPattern))
                return true;
        return false;
    }
}
