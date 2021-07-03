package indi.kurok1.pre.url.impl;

import indi.kurok1.pre.url.StringUtils;
import indi.kurok1.pre.url.URLPatternsMatcher;

import java.util.*;

/**
 * ANT风格匹配
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.03
 */
public class AntURLPatternsMatcher implements URLPatternsMatcher {


    /**
     * 匹配单字符串
     */
    public static final String ONE = "?";

    /**
     * 匹配0或者任意数量的字符
     */
    public static final String ANY = "*";

    /**
     * 匹配0或者更多的目录
     */
    public static final String MORE = "**";

    /**
     * Default path separator: "/"
     */
    public static final String DEFAULT_PATH_SEPARATOR = "/";

    /**
     * path separator
     */
    private String pathSeparator = DEFAULT_PATH_SEPARATOR;

    @Override
    public boolean matches(Collection<String> urlPatterns, String requestURI) {
        if (urlPatterns == null || urlPatterns.size() == 0)
            return false;

        for (String urlPattern : urlPatterns)
            if (match(urlPattern, requestURI))
                return true;

        return false;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    /**
     * 一条规则匹配uri
     * @param urlPattern non-null and not empty
     * @param requestURI non-null and not empty
     * @return 当前规则是否匹配成功
     */
    private boolean match(String urlPattern, String requestURI) {

        //拆分路径
        String[] paths = StringUtils.splitString(requestURI, pathSeparator);
        List<String> patterns = Arrays.asList(StringUtils.splitString(urlPattern, pathSeparator));

        int currentIndex = 0;

        //这里应该使用迭代器遍历
        Iterator<String> iterator = patterns.iterator();
        while (iterator.hasNext()) {
            String pattern = iterator.next();
            while (pattern == null || pattern.length() == 0)
                pattern = iterator.next();
            String path = paths[currentIndex];
            while (path == null || path.length() == 0) {
                currentIndex++;
                path = paths[currentIndex];
            }

            if ((pattern.contains(ONE) || pattern.contains(ANY)) && !MORE.equals(pattern)) {
                if (!matchAny(pattern, path))
                    return false;
                currentIndex++;
            } else if (MORE.equals(pattern)) {
                //如果是最后一个pattern，直接返回true
                if (!iterator.hasNext())
                    return true;

                String nextPattern = iterator.next();
                while (currentIndex < paths.length && !matchAny(nextPattern, paths[currentIndex])) {
                    currentIndex++;//当前path是满足 "**"
                }

                if (currentIndex == paths.length)
                    return false;

            } else if (!path.equals(pattern))
                return false;
        }

        return true;
    }

    /**
     * 匹配"*"或者"?"
     * @param pattern
     * @param path
     * @return
     */
    private boolean matchAny(final String pattern, final String path) {
        if (!path.equals(ANY) && path.length() != pattern.length())
            return false;//不包含*时，长度不一致必然匹配失败
        //?用于匹配单个字符,用这个分割是最好的
        String[] matchArray = StringUtils.splitString(pattern, ONE);
        int index = 0;
        try {
            for (String match : matchArray) {
                //检查是否包含*
                if (!match.contains(ANY)) {
                    //这种场景就很简单，保证一致就行
                    int length = match.length();
                    if (!match.equals(path.substring(index, index + length)))
                        return false;//子字符串匹配不一致，返回false
                    else {
                        index = index + length + 1;//index更新
                    }
                } else {
                    //foe example. pattern: aa*b*c  path:aaxxbxc
                    //todo 目前能想到的就path的每个字符遍历，或许有更高性能的算法？
                    char[] chars = path.toCharArray();
                    if (chars.length < match.length())
                        return false;
                    String[] realStr = StringUtils.splitString(match, ANY);
                    Queue<String> patternsQueue = new LinkedList<>();
                    for (String real : realStr) {
                        patternsQueue.offer(real);
                        patternsQueue.offer(ANY);
                    }


                    String real = null;
                    while ((real = patternsQueue.poll()) != null) {//队列非空时
                        if (ANY.equals(real)) {
                            String next = patternsQueue.poll();
                            if (next != null) {
                                String build = new String(chars, index, chars.length - index);
                                int found = build.lastIndexOf(next);
                                if (found == -1)
                                    return false;//未找到
                                index = found + next.length();
                            }
                        } else {
                            String build = new String(chars, index, index + real.length());
                            if (!real.equals(build))
                                return false;//不匹配
                            index = index + real.length();
                        }
                    }


                }

            }
        } catch (IndexOutOfBoundsException t) {
            return false;//越界，返回失败
        }
        return true;
    }

}
