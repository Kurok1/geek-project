## 通过 MicroProfile REST Client 实现 POST 接⼝去请求项⽬中的 ShutdownEndpoint，URI： http://127.0.0.1:8080/actuator/shutdown
## 可选：完善 my-rest-client 框架 POST ⽅法，实现 DefaultInvocationBuilder#buildPost ⽅法


### 对小马哥代码的补充
#### 1. `AnnotatedParamMetadata`增加`HeaderParam`,`BeanParam`的支持

增加支持注解[RequestTemplate](./src/main/java/indi/kurok1/microprofile/rest/RequestTemplate.java)
```java
public static Set<Class<? extends Annotation>> SUPPORTED_PARAM_ANNOTATION_TYPES =
            unmodifiableSet(new LinkedHashSet<>(asList(
                    PathParam.class,
                    QueryParam.class,
                    MatrixParam.class,
                    FormParam.class,
                    CookieParam.class,
                    HeaderParam.class,
                    BeanParam.class,
                    HeaderParam.class
            )));

/**
 * 只允许出现一次的注解
 */
public static Set<Class<? extends Annotation>> ANNOTATIONED_ONCE_TYPES = unmodifiableSet(new LinkedHashSet<>(asList(
        BeanParam.class
    )));

public RequestTemplate annotatedParamMetadata(AnnotatedParamMetadata annotatedParamMetadata) {
        Class<? extends Annotation> annotationType = annotatedParamMetadata.getAnnotationType();
        List<AnnotatedParamMetadata> metadataList = annotatedParamMetadataMap.computeIfAbsent(annotationType, type -> new LinkedList<>());
        if (ANNOTATIONED_ONCE_TYPES.contains(annotationType) && !metadataList.isEmpty())
            throw new IllegalArgumentException(String.format("the annotation @%s only marked once", annotationType.getName()));
        metadataList.add(annotatedParamMetadata);
        return this;
}
```

注解解析 [RestClientInterfaceInvocationHandler](./src/main/java/indi/kurok1/microprofile/rest/reflect/RestClientInterfaceInvocationHandler.java)
```java
    @Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    //todo 其他注解的解析。。。
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    //Handle @HeaderParam
    for (AnnotatedParamMetadata metadata : requestTemplate.getAnnotatedParamMetadata(HeaderParam.class)) {
        String paramName = metadata.getParamName();
        int paramIndex = metadata.getParameterIndex();
        Object paramValue = args[paramIndex];
        headers.add(paramName, paramValue);
    }
// @BeanParam 转换成Entity
    List<AnnotatedParamMetadata> beanParamMetaDatas = requestTemplate.getAnnotatedParamMetadata(BeanParam.class);
    Entity<?> entity = null;
    if (!beanParamMetaDatas.isEmpty()){
        AnnotatedParamMetadata metadata = beanParamMetaDatas.get(0);
        int index = metadata.getParameterIndex();
        Object arg = args[index];
        if (arg != null) {
            Parameter parameter=method.getParameters()[index];
            Set<String> produces=requestTemplate.getProduces();
            MediaType mediaType=null;
            if (produces.size() == 0 || produces.size() > 1) {
                mediaType=new MediaType();// */*
            } else {
                mediaType=MediaType.valueOf(produces.toArray(new String[1])[0]);
            }
            headers.add(HttpHeaders.CONTENT_TYPE,mediaType.toString());
            Variant variant=new Variant(mediaType,Locale.CHINA,"UTF-8");
            entity=Entity.entity(arg,variant,parameter.getDeclaredAnnotations());
        }
    }
    //todo 后续生成代理逻辑
    Invocation invocation = client.target(uri)
        .request(acceptedResponseTypes)
        .headers(headers)
        .build(httpMethod, entity);
    return invocation.invoke(returnType);
}
```

#### 2. http消息转换
由于`POST`接口往往涉及`HTTP BODY`，故新增`HttpBodyConverter`实现

合成`MessageBodyReader<T>, MessageBodyWriter<T>`
```java
public interface HttpBodyConverter<E, R> extends MessageBodyReader<E>, MessageBodyWriter<R> {


}
```
同时提供两种实现
* 基于`Jackson`的json消息转换 [JacksonJsonHttpBodyConverter](./src/main/java/indi/kurok1/rest/converter/JacksonJsonHttpBodyConverter.java)
* 基于`String`的简单文本消息转换 [StringHttpBodyConverter](./src/main/java/indi/kurok1/rest/converter/StringHttpBodyConverter.java)

同时提供`HttpBodyConverters`管理`HttpBodyConverter`
```java
public class HttpBodyConverters implements Iterable<HttpBodyConverter<?, ?>> {

   private final CopyOnWriteArrayList<HttpBodyConverter<?, ?>> loadedHttpBodyConverters = new CopyOnWriteArrayList<>();

    public HttpBodyConverters() {
        loadSpi();
    }

    public HttpBodyConverters(Collection<HttpBodyConverter<?, ?>> converters) {
        this();
        this.loadedHttpBodyConverters.addAll(converters);
    }

    private void loadSpi() {
        //todo 利用spi机制读取加载实现类
    }

    //查找合适的响应转换
    public HttpBodyConverter<?, ?> getReadableConverter(Class<?> type, Type genericType,
                                                               Annotation[] annotations, MediaType mediaType);

    //查找合适的请求转换
    public HttpBodyConverter<?, ?> getWriteableConverter(Class<?> type, Type genericType,
                                                        Annotation[] annotations, MediaType mediaType);

    public HttpBodyConverters add(int index, HttpBodyConverter<?, ?> converter);

    public HttpBodyConverters addFirst(HttpBodyConverter<?, ?> converter);

    public HttpBodyConverters addLast(HttpBodyConverter<?, ?> converter);

    /**
     * @return 默认消息转换器
     */
    public HttpBodyConverter<?, ?> getDefault();
}
```

#### 3. `DefaultResponse`增加消息转换 [DefaultResponse](./src/main/java/indi/kurok1/rest/core/DefaultResponse.java)
```java
@Override
public <T> T readEntity(Class<T> entityType) {
    try {
        InputStream inputStream = connection.getInputStream();
        // 参考 HttpMessageConverter 实现，实现运行时动态判断
        if (converter != null) {
            if (converter.isReadable(entityType, entityType.getGenericSuperclass(), annotations, mediaType)) {
                this.entity = converter.readFrom(entityType, entityType.getGenericSuperclass(), annotations, mediaType, null, inputStream);
                return (T) this.entity;
            }
        }
        if (String.class.equals(entityType)) {
            Object value = IOUtils.toString(inputStream, encoding);
            this.entity = value;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            this.entity = objectMapper.readValue(new InputStreamReader(inputStream, encoding), entityType);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    } finally {
        connection.disconnect();
    }
    return (T) this.entity;
}
```

### POST请求实现
具体实现文件 [HttpPostInvocation](./src/main/java/indi/kurok1/rest/client/HttpPostInvocation.java)

核心api说明
* indi.kurok1.rest.client.HttpPostInvocation#setRequestHeaders(HttpURLConnection) 写入HTTP请求头
* indi.kurok1.rest.client.HttpPostInvocation#sendRequest 发送HTTP请求,不涉及解析
* indi.kurok1.rest.client.HttpPostInvocation#writeEntity 写入HTTP请求体,利用`HttpBodyConverter`
* indi.kurok1.rest.client.HttpPostInvocation#invoke(java.lang.Class<T>) 获取HTTP响应，并解析返回结果
* indi.kurok1.rest.client.HttpPostInvocation#submit 异步请求，返回响应封装`DefaultResponse` 
* indi.kurok1.rest.client.HttpPostInvocation#submit(java.lang.Class<T>) 异步请求，返回响应结果


### 远程接口定义和测试
```java
public interface RemoteShutdownApi {

    @POST
    @Path("/actuator/shutdown")
    public void shutdown();

    @POST
    @Path(("/api/user/save"))
    @Produces("application/json")
    public User save(@BeanParam User user);

}

public static void main(String[] args) throws Exception {
    RemoteShutdownApi service = RestClientBuilder.newBuilder()
                .baseUrl(new URL("http://127.0.0.1:8080"))
                .build(RemoteShutdownApi.class);
    User user = new User();
    user.setName("aa");
    System.out.println(service.save(user));
    
    service.shutdown();
}
```