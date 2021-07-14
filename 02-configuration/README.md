## 在 my-configuration基础上，实现ServletRequest请求参数的 ConfigSource（MicroProfile Config）

### 简单实现`Microprofile Config`
#### 1.实现`ConfigSources`
这里简单参考`Spring`的`PropertySources`实现，[ConfigSources](./src/main/java/indi/kurok1/configuration/source/ConfigSources.java)
```java
public class ConfigSources implements Iterable<ConfigSource> {

    private final List<ConfigSource> sourceList = new CopyOnWriteArrayList<>();

    @Override
    public Iterator<ConfigSource> iterator();

    @Override
    public void forEach(Consumer<? super ConfigSource> action);

    @Override
    public Spliterator<ConfigSource> spliterator();

    public void addFirst(ConfigSource configSource);

    public void addLast(ConfigSource configSource);

    public void add(int position, ConfigSource configSource);

    public void with(Collection<ConfigSource> configSources);

    public void release(String name);

}
```

#### 2.实现`org.eclipse.microprofile.config.Config`
这里参考小马哥代码 [DefaultConfig](./src/main/java/indi/kurok1/configuration/DefaultConfig.java)
```java
class DefaultConfig implements Config {

    private final ConfigSources configSources;

    private final Converters converters;
    
    //overrides...
}
```

#### 3.实现`org.eclipse.microprofile.config.spi.ConfigProviderResolver`
这里同样参考小马哥的实现 [DefaultConfigProviderResolver](./src/main/java/indi/kurok1/configuration/DefaultConfigProviderResolver.java)

同时编写`services`文件，方便spi注入 [ConfigProviderResolver](./src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigProviderResolver)

#### 4.实现`org.eclipse.microprofile.config.spi.Converter`
这里先拓展`Converter`接口，使其具备顺序能力
```java
public interface OrderedConverter<T> extends Converter<T> {

    int getOrder();

    int MAX_ORDERED = 0;
    int MIN_ORDERED = Integer.MIN_VALUE;
    int DEFAULT_ORDERED = 100;

}
```
然后基于`OrderedConverter`，实现不同类型的转换:
* 字符串默认转换 [DefaultConverter](./src/main/java/indi/kurok1/configuration/converter/DefaultConverter.java)
* 整数类型转换 [IntegerConverter](./src/main/java/indi/kurok1/configuration/converter/IntegerConverter.java)
* 小数类型转换 [BigDecimalConverter](./src/main/java/indi/kurok1/configuration/converter/BigDecimalConverter.java)
* 根据分隔符切割成字符串数组转换 [StringArrayConverter](./src/main/java/indi/kurok1/configuration/converter/StringArrayConverter.java)

根据spi注入方式整合所有的`Converter`到[Converters](./src/main/java/indi/kurok1/configuration/converter/Converters.java)

#### 5.实现`org.eclipse.microprofile.config.spi.ConfigBuilder`
同样参考了小马哥的实现 [DefaultConfigBuilder](./src/main/java/indi/kurok1/configuration/DefaultConfigBuilder.java)

### 实现`ServletRequestConfiguration`
实现代码[ServletRequestConfiguration](./src/main/java/indi/kurok1/configuration/source/ServletRequestConfiguration.java)
```java
public class ServletRequestConfiguration implements ConfigSource {

    private final ServletRequest servletRequest;

    private final String name;

    public ServletRequestConfiguration(String name, ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
        this.name = name;
    }
    
    //overrides...
}
```

### 与`ServletRequest`生命周期绑定
在这里，`Config`作为获取配置的核心api,由多个`ConfigSource`绑定，每一次的`ServletRequest`相当于一个`ConfigSource`

因此，`ServletRequest`的初始化和销毁，对应`ConfigSource`的绑定和解绑。

同理，`ServletContext`的初始化和销毁。也对应着`Config`的绑定和解绑。

基于这种设计思路，我决定利用`Servlet`规范中的事件驱动机制，完成`Config`和`Servlet`的生命周期绑定

#### `Config`绑定`ServletContext`生命周期
[ConfigProviderInitializer](./src/main/java/indi/kurok1/configuration/listener/ConfigProviderInitializer.java)
```java
public class ConfigProviderInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Thread.currentThread().setContextClassLoader(sce.getServletContext().getClassLoader());
        ConfigProviderResolver resolver = ConfigProviderResolver.instance();
        Config config = resolver.getBuilder().forClassLoader(Thread.currentThread().getContextClassLoader())
                .addDefaultSources().addDiscoveredSources().addDiscoveredConverters().build();
        resolver.registerConfig(config, Thread.currentThread().getContextClassLoader());
        ConfigProviderResolver.setInstance(resolver);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ConfigProviderResolver resolver = ConfigProviderResolver.instance();
        Config config = resolver.getConfig(context.getClassLoader());
        resolver.releaseConfig(config);
    }
}
```
初始化完成时,利用`ServletContext#getClassLoader()`注册`Config`到`ConfigProviderResolver`。

销毁完成时，解除`Config`的绑定

#### `ConfigSource`绑定`ServletRequest`生命周期
[ServletRequestConfigurationRegistrar](./src/main/java/indi/kurok1/configuration/listener/ServletRequestConfigurationRegistrar.java)
```java
public class ServletRequestConfigurationRegistrar implements ServletRequestListener {

    public final String CONFIG_NAME = "indi.kurok1.configuration.servlet.name";

    private ConfigProviderResolver getConfigProviderResolver() {
        return ConfigProviderResolver.instance();
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        //解除绑定
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        //绑定configsource...
    }

    private String generateConfigName() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return String.format("servlet-%s", uuid);
    }
}
```
这里利用`UUID`为每次请求生成唯一的`config name`, 同时利用`ServletRequest#setAttribute`绑定`config name`

初始化请求时，获取到当前的`Config`，生成`ServletRequestConfiguration`,并作为`ConfigSource`注册到`Config`

请求销毁时，获取到当前的`Config`，利用`ServletRequest#getAttribute`解绑对应的`ConfigSource`