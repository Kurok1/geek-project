package indi.kurok1.pre.url.impl;

import indi.kurok1.pre.url.URLPatternsMatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * URLPatternsMatcher，组合模式实现
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.03
 * @see indi.kurok1.pre.url.URLPatternsMatcher
 */
public class CompositeURLPatternsMatcher implements URLPatternsMatcher {

    private List<URLPatternsMatcher> subordinates = new CopyOnWriteArrayList<>();

    public CompositeURLPatternsMatcher() {
        init();
    }

    public void addURLPatternsMatcher(URLPatternsMatcher matcher) {
        if (matcher == null)
            throw new NullPointerException();

        subordinates.add(matcher);
    }

    private void init() {
        //2.通过ServiceLoader注入URL解析
        ServiceLoader<URLPatternsMatcher> load = ServiceLoader.load(URLPatternsMatcher.class);
        Iterator<URLPatternsMatcher> iterator = load.iterator();
        while (iterator.hasNext())
            subordinates.add(iterator.next());

        if (subordinates.size() == 0) {
            //没有指定文件?给一个默认实现
            subordinates.add(new AntURLPatternsMatcher());
        }
    }

    @Override
    public boolean matches(Collection<String> urlPatterns, String requestURI) {
        for (URLPatternsMatcher matcher : subordinates) {
            if (matcher.matches(urlPatterns, requestURI))
                return true;//只要有一个匹配上了，就返回成功
        }
        return false;
    }
}
