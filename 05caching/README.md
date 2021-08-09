## 参考实现类org.geektimes.cache.interceptor.CachePutInterceptor，实现@javax.cache.annotation.CacheRemove 注解的 @Interceptor Class

由于作业写太晚了，fork的时候发现小马哥已经实现，故这里分析小马哥的实现
### 1.抽象注解解析 [CacheOperationAnnotationInfo](./src/main/java/indi/kurok1/cache/annotation/util/CacheOperationAnnotationInfo.java)
```java
public class CacheOperationAnnotationInfo {

    private static Class<? extends Throwable>[] EMPTY_FAILURE = new Class[0];

    private final String cacheName;

    private final Boolean afterInvocation;

    private final Class<? extends CacheResolverFactory> cacheResolverFactoryClass;

    private final Class<? extends CacheKeyGenerator> cacheKeyGeneratorClass;

    private final Class<? extends Throwable>[] appliedFailures;

    private final Class<? extends Throwable>[] nonAppliedFailures;

    private final Boolean skipGet;

    private final String exceptionCacheName;
    
    //constructor...
    //getter... setter...
}
```
这里定义了`@CachePut`,`@CacheRemove`等注解的通用属性，后续利用Interceptor拦截方法的时候会解析注解

### 2. Java Caching API实现
这里主要实现的是参数和方法上下文
* `CacheMethodDetails` 这个api主要存储作用于方法上的注解，并解析出`CacheName`，实现：[ReflectiveCacheMethodDetails](./src/main/java/indi/kurok1/cache/annotation/ReflectiveCacheMethodDetails.java)
* `CacheInvocationParameter` 这个api存储方法的传参，并解析注解，一个方法拥有多个`CacheInvocationParameter`，实现 [ReflectiveCacheInvocationParameter](./src/main/java/indi/kurok1/cache/annotation/ReflectiveCacheInvocationParameter.java)
* `CacheInvocationContext` 整合`CacheInvocationParameter`和`CacheMethodDetails`，包含实际执行的对象, 实现[ReflectiveCacheInvocationContext](./src/main/java/indi/kurok1/cache/annotation/ReflectiveCacheInvocationContext.java)
* `CacheKeyInvocationContext` 这个api存储`CacheKey`和`CacheValue`对应的参数，允许存在多个`CacheKey`，只能存在一个`CacheValue`, 实现[ReflectiveCacheKeyInvocationContext](./src/main/java/indi/kurok1/cache/annotation/ReflectiveCacheKeyInvocationContext.java)

注意，这里的api只负责解析被`@CacheResult`,`@CachePut`,`@CacheRemove`,`CacheRemoveAll`修饰的方法和参数
  
### 3. 抽象拦截器实现 [CacheOperationInterceptor](./src/main/java/indi/kurok1/cache/annotation/interceptor/CacheOperationInterceptor.java)
```java
public abstract class CacheOperationInterceptor<A extends Annotation> extends AnnotatedInterceptor<A> {

    private final ConcurrentMap<A, CacheResolverFactory> cacheResolverFactoryCache = new ConcurrentHashMap<>();

    private final ConcurrentMap<A, CacheKeyGenerator> cacheKeyGeneratorCache = new ConcurrentHashMap<>();

    protected Object execute(InvocationContext context, A cacheOperationAnnotation) throws Throwable;

    protected abstract CacheOperationAnnotationInfo getCacheOperationAnnotationInfo(A cacheOperationAnnotation, CacheDefaults cacheDefaults);

    protected abstract Object beforeExecute(A cacheOperationAnnotation, CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                                            CacheOperationAnnotationInfo cacheOperationAnnotationInfo,
                                            Cache cache, Optional<GeneratedCacheKey> cacheKey);

    protected abstract void afterExecute(A cacheOperationAnnotation, CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                                         CacheOperationAnnotationInfo cacheOperationAnnotationInfo,
                                         Cache cache, Optional<GeneratedCacheKey> cacheKey, Object result);

    protected abstract void handleFailure(A cacheOperationAnnotation, CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                                          CacheOperationAnnotationInfo cacheOperationAnnotationInfo,
                                          Cache cache, Optional<GeneratedCacheKey> cacheKey, Throwable failure);

    private Cache resolveCache(A cacheOperationAnnotation, CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                               CacheOperationAnnotationInfo cacheOperationAnnotationInfo);

    protected CacheResolverFactory getCacheResolverFactory(A cacheOperationAnnotation,
                                                           CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                                                           CacheOperationAnnotationInfo cacheOperationAnnotationInfo);

    private Optional<GeneratedCacheKey> generateCacheKey(A cacheOperationAnnotation,
                                                         CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                                                         CacheOperationAnnotationInfo cacheOperationAnnotationInfo);

    private CacheKeyGenerator getCacheKeyGenerator(A cacheOperationAnnotation,
                                                   CacheKeyInvocationContext<A> cacheKeyInvocationContext,
                                                   CacheOperationAnnotationInfo cacheOperationAnnotationInfo);

    private boolean shouldHandleFailure(Throwable failure, CacheOperationAnnotationInfo cacheOperationAnnotationInfo);
}

```
基于`AnnotatedInterceptor`作拦截,保留四个抽线方法实现
* `getCacheOperationAnnotationInfo`，解析注解,返回`CacheOperationAnnotationInfo`
* `beforeExecute` 先行执行部分，除`@CahceResult`外，都基于`afterInvocation()`控制，`fasle`时执行
* `afterExecute` 后续执行部分，除`@CahceResult`外，都基于`afterInvocation()`控制，`true`时执行
* `handleFailure` 异常处理

核心方法`execute`说明：
1. 解析方法参数，方法注解，检查是否存在`@CacheDefaults`，存在则设置默认值
2. 利用`resolveCache`解析出实际`Cache`实现
3. 生成`GeneratedCacheKey`，前提是没有指定`@CacheKey`
4. 顺序调用`beforeExecute()`, `javax.interceptor.InvocationContext.proceed()`, `afterExecute()`,出现异常则处理

### 4. 缓存实现
* `CachePut`[CachePutInterceptor](./src/main/java/indi/kurok1/cache/annotation/interceptor/CachePutInterceptor.java)
* `CacheRemove`[CachePutInterceptor](./src/main/java/indi/kurok1/cache/annotation/interceptor/CacheRemoveInterceptor.java)
* `CacheRemoveAll`[CacheRemoveAllInterceptor](./src/main/java/indi/kurok1/cache/annotation/interceptor/CacheRemoveAllInterceptor.java)
* `CacheResult`[CacheResultInterceptor](./src/main/java/indi/kurok1/cache/annotation/interceptor/CacheResultInterceptor.java)
