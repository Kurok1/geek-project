## 拓展`org.geektimes.http.server.jdk.servlet.URLPatternsMatcher`接口，实现 ANT 风格语义，可以参考 Spring
`org.springframework.security.web.util.matcher.AntPathRequestMatcher`

### 接口定义
[URLPatternsMatcher](./src/main/java/indi/kurok1/pre/url/URLPatternsMatcher.java)
```java
public interface URLPatternsMatcher {

    boolean matches(Collection<String> urlPatterns, String requestURI);

}
```

### 拓展实现 [AntURLPatternsMatcher](./src/main/java/indi/kurok1/pre/url/impl/AntURLPatternsMatcher.java)
Ant风格样例

| URL路径              | 说明                                                         |
| -------------------- | ------------------------------------------------------------ |
| `/app/*.x`           | 匹配所有在app路径下的.x文件                                  |
| `/app/p?ttern`       | 匹配/app/pattern 和 /app/pXttern,但是不包括/app/pttern       |
| `/**/example`        | 匹配/app/example, /app/foo/example, 和 /example              |
| `/app/**/dir/file.*` | 匹配/app/dir/file.jsp, /app/foo/dir/file.html,/app/foo/bar/dir/file.pdf, 和 /app/dir/file.java |
| `/**/*.jsp`          | 匹配任何的.jsp 文件                                          |


## 将多种 URLPatternsMatcher 组成一种，方便不 同的 URL Pattern 模式来匹配 (组合模式实现)
#### URLPatternsMatcher多实现
[AntURLPatternsMatcher](./src/main/java/indi/kurok1/pre/url/impl/AntURLPatternsMatcher.java)

[EqualsURLPatternsMatcher](./src/main/java/indi/kurok1/pre/url/impl/EqualsURLPatternsMatcher.java)
#### 组合器
[CompositeURLPatternsMatcher](./src/main/java/indi/kurok1/pre/url/impl/CompositeURLPatternsMatcher.java)
```java
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
```

#### 通过`ServiceLoader`方式注入
[服务描述文件](./src/main/resources/META-INF/services/indi.kurok1.pre.url.URLPatternsMatcher)
```text
### ./resources/META-INF/services/indi.kurok1.pre.url.URLPatternsMatcher
indi.kurok1.pre.url.impl.AntURLPatternsMatcher
indi.kurok1.pre.url.impl.EqualsURLPatternsMatcher
```
