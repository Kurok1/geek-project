## 将 my-interceptor 工程代码增加 JDK 动态代理，将 @BulkHead 等注解标注在接口上，实现方法拦截。

### `InvocationContext`实现

[`JdkProxyInvocationContext`](./src/main/java/indi/kurok1/interceptor/jdkProxy/JdkProxyInvocationContext.java)

```java
public class JdkProxyInvocationContext implements InvocationContext {

    private final Object target;

    private final Method method;

    private Object[] parameters = new Object[0];

    private final Map<String, Object> contextData;

    //todo... other getter implements

    @Override
    public Object proceed() throws Exception {
        return method.invoke(target, parameters);
    }
}
```

### `InvocationHandler`实现

[`InterceptorInvocationHandler`](./src/main/java/indi/kurok1/interceptor/jdkProxy/JdkProxyInvocationContext.java)



###  `Interceptor`动态载入

现有基础上`Interceptor`的载入是静态，以手动的方式注入`classpath`下所有的`Interceptor`，然后在`Interceptor`内部拦截过程中判断注解，决定是否拦截。

为了减少`Interceptor`的冗余代码，实现`Interceptor`动态载入

#### `AbstractInterceptorRegistry`

定义抽象`Interceptor`注册中心，[AbstractInterceptorRegistry](./src/main/java/indi/kurok1/interceptor/AbstractInterceptorRegistry.java)

```java
/**
 * 拦截器注册中心
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 * @param <K> 用于标识拦截器的泛型
 */
public abstract class AbstractInterceptorRegistry<K> extends ConcurrentHashMap<K, Object> {
    
    /**
     * 保留方法,加载拦截器
     */
    protected abstract void load();


    /**
     * 要求一个拦截器必须被 {@link javax.interceptor.InterceptorBinding}
     * 同时必须存在一个方法满足 {@link AbstractInterceptorRegistry#isInterceptMethod(java.lang.reflect.Method)}
     * @param interceptor 拦截器实例
     * @return 是否满足条件
     */
    protected boolean isValidateInterceptor(Object interceptor);
    
    /**
     * 要求该方法被 {@link javax.interceptor.AroundInvoke} 标记，并且有且只有一个参数，其参数类型为 {@link javax.interceptor.InvocationContext}
     * @param method 目标方法
     * @return 是否满足条件
     */
    protected boolean isInterceptMethod(Method method);


    public Optional<Object> getInterceptor(Class clazz) {
        if (isEmpty())
            return Optional.empty();

        return Stream.of(values().toArray())
                .filter(clazz::equals)
                .findFirst();

    }
}
```

其中，泛型参数`K`代表标识符的类型，一个标识符可以代表一个唯一的`Interceptor`



#### 基于`AnnotatedInterceptor`的`AnnotatedInterceptorRegistry`实现

代码实现[AnnotatedInterceptorRegistry](./src/main/java/indi/kurok1/interceptor/AnnotatedInterceptorRegistry.java)

```java
public final class AnnotatedInterceptorRegistry extends AbstractInterceptorRegistry<String> {

    private final CopyOnWriteArrayList<Class<? extends Annotation>> supportAnnotationTypes = new CopyOnWriteArrayList<>(Arrays.asList(
            Asynchronous.class, Bulkhead.class, CircuitBreaker.class, Fallback.class, Retry.class, Timeout.class
    ));

    public void addSupportAnnotationTypes(Class<? extends Annotation> newSupportAnnotation) {
        this.supportAnnotationTypes.addIfAbsent(newSupportAnnotation);
    }

    public boolean isSupport(Class<? extends Annotation> supportAnnotation) {
        return this.supportAnnotationTypes.contains(supportAnnotation);
    }

    public AnnotatedInterceptorRegistry() {
        load();
    }

    @Override
    protected void load() {
        //spi加载, 唯一标识符为注解的全限定名称
    }
}
```

#### 动态注入

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    List<Object> resolvedInterceptors = new LinkedList<>();
    Annotation[] annotations = method.getDeclaredAnnotations();
    JdkProxyInvocationContext invocationContext = new JdkProxyInvocationContext(this.target, method, Collections.emptyMap());
    invocationContext.setParameters(args);
    //查找是否指定了拦截器
    for (Annotation annotation : annotations) {
        if (isInterceptors(annotation.getClass())) {
            Interceptors interceptors = (Interceptors) annotation;
            if (interceptors.value().length > 0) {
                for (Class clazz : interceptors.value()) {
                    Object interceptor = this.registry.getInterceptor(clazz);
                    if (interceptor != null && !resolvedInterceptors.contains(interceptor))
                        resolvedInterceptors.add(interceptor);
                }
            }
            continue;
        }

        Object interceptor = this.registry.get(annotation.annotationType().getName());
        if (interceptor != null && !resolvedInterceptors.contains(interceptor))
            resolvedInterceptors.add(interceptor);
    }
    //没有指定拦截器，直接执行
    if (resolvedInterceptors.isEmpty()) {
        return invocationContext.proceed();
    }

    //制定了拦截器，链式调用
    Object[] array = resolvedInterceptors.toArray();
    //排序
    Arrays.sort(array, Prioritized.COMPARATOR);
    ChainableInvocationContext context = new ChainableInvocationContext(invocationContext, array);
    return context.proceed();
}
```



### 代码测试

#### 自定义拦截器

`@Notify`

```java
/**
 * 执行完成后，执行一个通知操作
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 * @see InterceptorBinding
 * @see System
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@InterceptorBinding
@Inherited
public @interface Notify {

    String value() default "";

}
```

代码实现 [`NotifyInterceptor`](./src/main/java/indi/kurok1/impl/NotifyInterceptor.java)



#### 测试接口

```java
public interface TestService {

    @Retry(maxRetries = 3,
            delay = 0, maxDuration = 0, jitter = 0,
            retryOn = UnsupportedOperationException.class)
    @Notify(value = "Hello")
    void doSomething();

}
```

#### 测试代码

```
@Test
public void test() {
    AnnotatedInterceptorRegistry registry = new AnnotatedInterceptorRegistry();
    registry.addSupportAnnotationTypes(Notify.class);
    Class[] classes = new Class[]{TestService.class};
    TestService instance = (TestService) Proxy.newProxyInstance(getClass().getClassLoader(), classes, new InterceptorInvocationHandler(registry, new TestServiceImpl()));
    instance.doSomething();
}

static class TestServiceImpl implements TestService {
    @Override
    public void doSomething() {
        System.out.println("working!!!");
    }
}
```

