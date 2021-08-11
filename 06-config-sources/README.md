## 增加一个注解名为 @ConfigSources，使其能够关联多个 @ConfigSource，并且在 @ConfigSource 使用 Repeatable；可以对比参考 Spring 中 @PropertySources 与 @PropertySource，并且文字说明 Java 8 @Repeatable 实现原理。
可选作业，根据 URL 与 URLStreamHandler 的关系，扩展一个自定义协议，可参考 sun.net.www.protocol.classpath.Handler

### `@ConfigSources`注解定义
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigSources {

    ConfigSource[] value();

}
```
同时`@ConfigSource`支持`@Repeatable`
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ConfigSources.class)
public @interface ConfigSource
```

### 加载`@ConfigSources`
`@ConfigSources`的语义为多个`@ConfigSource`的组合。而单个`@ConfigSource`定义了配置源的加载路径。

因此需要`Config`主动探测当前`classpath`下所有的`@ConfigSource`,并完成`ConfigSource`的加载

#### 1.拓展`DefaultConfigBuilder`
`DefaultConfigBuilder#addDiscoveredSources`要求主动探测配置源并添加，因此拓展该实现

[AnnotationConfigBuilder](./src/main/java/org/geektimes/configuration/microprofile/config/AnnotationConfigBuilder.java)
```java
public class AnnotationConfigBuilder extends DefaultConfigBuilder {

    private final String basePackage;
    private final Class<?> source;
    private final ConfigSourcesScanner scanner;

    public AnnotationConfigBuilder(Class<?> sourceCLass, ClassLoader classLoader) {
        super(classLoader);
        this.source = sourceCLass;
        this.basePackage = resolveSourceClassPackage(sourceCLass);
        this.scanner = new ConfigSourcesScanner(getConfigSources(), classLoader);
    }

    private static String resolveSourceClassPackage(Class<?> sourceClass) {
        return sourceClass.getPackage().getName();
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        super.addDiscoveredSources();
        this.scanner.scan(this.basePackage);
        return this;
    }
}
```
在这里，需要外部传递一个根类，以这个根类所在的包为基准，**递归**扫描改包下所有的类，检查是否存在`@ConfigSource`或`@ConfigSources`，存在则完成加载

#### 2.扫描包,加载`ConfigSource`
[ConfigSourcesScanner](./src/main/java/org/geektimes/configuration/microprofile/config/discover/ConfigSourcesScanner.java)

该类的作用是扫描指定包下所有的类，根据`@ConfigSources`，`@ConfigSource`指定的配置源位置，加载配置源到`ConfigSources`
```java
public class ConfigSourcesScanner {

    private ConfigSource[] resolveConfigSources(ConfigSources configSources) {
        if (configSources != null) {
            return configSources.value();
        }
        return null;
    }

    private final org.geektimes.configuration.microprofile.config.source.ConfigSources configSources;
    private final ConfigSourcesLoader loader;
    private final ClassLoader classLoader;

    public ConfigSourcesScanner(org.geektimes.configuration.microprofile.config.source.ConfigSources configSources, ClassLoader classLoader) {
        this.configSources = configSources;
        this.classLoader = classLoader;
        this.loader = new ConfigSourcesLoader(classLoader);
    }

    /**
     * 扫描指定包下的所有类
     * @param basePackageName 指定包
     */
    public final void scan(String basePackageName);

    /**
     * 扫描路径下的类文件，递归扫描子文件夹
     * @param basePath 文件系统的路径
     * @param classLoader 类加载器
     */
    protected void scanInternal(String basePath, ClassLoader classLoader);

    /**
     * 解析一个类，并且加载配置
     * @param className 类的全限定名称
     */
    protected final void resolveClass(String className) throws ClassNotFoundException;

    /**
     * 批量加载{@link ConfigSource}
     * @param configSources 作用于类上的ConfigSource集合
     */
    private void loadConfigSources(ConfigSource[] configSources);

    /**
     * 加载单个{@link ConfigSource}，
     * @see ConfigSourceFactory
     * @param configSource 目标类上的ConfigSource
     */
    protected void loadConfigSource(ConfigSource configSource);

}
```

#### 3.`ConfigSource`的创建
`ConfigSource`的创建是基于`ConfigSourceFactory`来实现的。

同时注意到`@ConfigSource#resource()`返回了配置源的路径，这是一个`URL`地址，因此可能会出现多种不同协议的`URL`地址，所以需要针对不同协议实现不同的`ConfigSourceFactory`。

同时多个`ConfigSourceFactory`之间使用**组合模式**的的方式进行管理([ConfigSourcesLoader](./src/main/java/org/geektimes/configuration/microprofile/config/discover/ConfigSourcesLoader.java))。
因此有必要拓展下`ConfigSourceFactory`接口
```java
public interface ConfigSourceFactory {

    /**
     * Create a new {@link org.eclipse.microprofile.config.spi.ConfigSource} instance
     *
     * @param name     {@link ConfigSource#getName()}
     * @param ordinal  {@link ConfigSource#getOrdinal()}
     * @param resource the {@link URL} for the content of {@link ConfigSource}
     * @param encoding the encoding of the content of resource
     * @return {@link org.eclipse.microprofile.config.spi.ConfigSource}
     */
    org.eclipse.microprofile.config.spi.ConfigSource createConfigSource(String name, int ordinal, URL resource, String encoding);

    /**
     * Determine whether the current resource supports loading
     * @param name {@link ConfigSource#getName()}
     * @param resource the {@link URL} for the content of {@link ConfigSource}
     * @return can loading if true
     */
    boolean isSupport(String name, URL resource);

    /**
     * auto generate config source name when name is not set
     * @param url url
     * @return 配置源名称
     */
    default String generateConfigSourceName(URL url) {
        return String.format("%s@%d", url.toString(), System.identityHashCode(url));
    }
}
```
* `createConfigSource` 为创建`ConfigSource`的核心方法
* `isSupport` 提供前置判断，确保传递的`URL`地址能够正确的被解析
* `generateConfigSourceName` 配置源名称生成的兜底方法，当没有指定`@ConfigSource#name()`时可以用该方法自动生成name

同时为了提供代码复用率,提供了一个基于网络协议的抽象`ConfigSourceFactory`实现[ProtocolBasedConfigSourceFactory](./src/main/java/org/geektimes/configuration/microprofile/config/discover/ProtocolBasedConfigSourceFactory.java)
```java
public abstract class ProtocolBasedConfigSourceFactory implements ConfigSourceFactory {


    public ProtocolBasedConfigSourceFactory() {
        String protocol = getSupportedProtocol();
        if (protocol == null || protocol.isEmpty())
            throw new NullPointerException();
    }

    /**
     * @return 当前`ConfigSourceFactory`支持的协议
     */
    public abstract String getSupportedProtocol();

    @Override
    public boolean isSupport(String name, URL resource) {
        return resource.getProtocol().equals(getSupportedProtocol());
    }
}
```
以此为基础，实现了多种协议下的`ConfigSourceFactory`
* [ClassPathPropertiesConfigSourceFactory](./src/main/java/org/geektimes/configuration/microprofile/config/discover/classpath/ClassPathPropertiesConfigSourceFactory.java) 基于[classpath](./src/main/java/sun/net/www/protocol/classpath/Handler.java) 协议，加载`.properties`文件作为`ConfigSource`
* [FileSystemPropertiesConfigSourceFactory](./src/main/java/org/geektimes/configuration/microprofile/config/discover/fs/FileSystemPropertiesConfigSourceFactory.java) 基于[fs](./src/main/java/sun/net/www/protocol/fs/Handler.java) 协议，加载`.properties`文件作为`ConfigSource`,`fs`协议使用绝对路径
* [JsonPropertiesConfigSourceFactory](./src/main/java/org/geektimes/configuration/microprofile/config/discover/json/JsonConfigSourceFactory.java) 基于[json](./src/main/java/sun/net/www/protocol/json/Handler.java) 协议，利用`Jackson`加载`.json`文件作为`ConfigSource`,使用绝对路径

### `@ConfigSources`加载测试
测试配置文件

[app.json](./src/main/resources/app.json)

[app.properties](./src/main/resources/app.properties)

测试类
```java
@ConfigSource(
        resource = "fs://F:/learning/geek-project/06-config-sources/src/main/resources/app.properties",
        ordinal = 10000
)
@ConfigSource(
        resource = "json://F:/learning/geek-project/06-config-sources//src/main/resources/app.json",
        ordinal = 10010
)
public class ConfigSourcesTest {

    public static void main(String[] args) {
        DefaultConfigProviderResolver resolver = new DefaultConfigProviderResolver();
        resolver.configureConfigBuilder(new AnnotationConfigBuilder(ConfigSourcesTest.class, Thread.currentThread().getContextClassLoader()));
        ConfigProviderResolver.setInstance(resolver);
        Config config = ConfigProvider.getConfig();
        Integer age = config.getValue("kurok1.age", Integer.class);
        String name = config.getValue("key2.name1", String.class);
        System.out.println("load age from config : " + age);
        System.out.println("load name from config : " + name);
    }

}
```
测试结果输出
```shell
load age from config : 11
load name from config : b
```

### `@Repeatable`实现
`@Repeatable`作为Java8的新特性，本质上还是一种语法糖，可以通过对比字节码文件得到验证
使用`@ConfigSources`
```java
@ConfigSources(
        value = {
                @ConfigSource(
                        resource = "fs://F:/github/geek-project/06-config-sources/src/main/resources/app.properties",
                        ordinal = 10000
                ),
                @ConfigSource(
                        resource = "json://F:/github/geek-project/06-config-sources//src/main/resources/app.json",
                        ordinal = 10010
                )
        }
)
public class ConfigSourcesTest
```
对应字节码关联部分
```text
@Lorg/geektimes/configuration/microprofile/config/annotation/ConfigSources;(value={@Lorg/geektimes/configuration/microprofile/config/annotation/ConfigSource;(resource="fs://F:/github/geek-project/06-config-sources/src/main/resources/app.properties", ordinal=10000), @Lorg/geektimes/configuration/microprofile/config/annotation/ConfigSource;(resource="json://F:/github/geek-project/06-config-sources//src/main/resources/app.json", ordinal=10010)})
```

使用多个`@ConfigSource`
```java
@ConfigSource(
        resource = "fs://F:/learning/geek-project/06-config-sources/src/main/resources/app.properties",
        ordinal = 10000
)
@ConfigSource(
        resource = "json://F:/learning/geek-project/06-config-sources//src/main/resources/app.json",
        ordinal = 10010
)
public class ConfigSourcesTest
```
对应字节码文件关联部分
```text
@Lorg/geektimes/configuration/microprofile/config/annotation/ConfigSources;(value={@Lorg/geektimes/configuration/microprofile/config/annotation/ConfigSource;(resource="fs://F:/github/geek-project/06-config-sources/src/main/resources/app.properties", ordinal=10000), @Lorg/geektimes/configuration/microprofile/config/annotation/ConfigSource;(resource="json://F:/github/geek-project/06-config-sources//src/main/resources/app.json", ordinal=10010)})
```
可以看到，不管使用什么方式，最终生成的字节码文件是一样的。只是看起来后者更简洁优雅点罢了
