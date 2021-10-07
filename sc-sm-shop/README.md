将项目的中 Zipkin 客户端（ServiceClientApplication 和 ServiceProviderApplication）与 Zipkin 服务端整合起来
可选：
写出 Zipkin 客户端和服务端之间数据传输的过程以及细节
了解 Zipkin 如何实现分布式数据采集和存储
---
增加依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```


## 写出 Zipkin 客户端和服务端之间数据传输的过程以及细节
在Brave中，对`Span`的处理是由`SpanHandler`完成，因此猜想zipkin在上传Trace的实现的逻辑中，必然针对`SpanHandler`进行了适配。通过查询`SpanHandler`的实现类，
找到了Zipkin的的适配实现
```java
public class ZipkinSpanHandler extends SpanHandler implements Closeable {
  public static SpanHandler create(Reporter<Span> spanReporter) {
    return newBuilder(spanReporter).build();
  }
  
  public static Builder newBuilder(Reporter<Span> spanReporter) {
    if (spanReporter == null) throw new NullPointerException("spanReporter == null");
    return new ConvertingZipkinSpanHandler.Builder(spanReporter);
  }
  
}
```
可以看到`ZipkinSpanHandler`进行适配的同时依赖外部传递的`Reporter`,再来看下`Reporter`的几个主要实现
* `ConvertingSpanReporter` 将Brave的`Span`与Zipkin的`Span`进行转换，装饰器模式，依赖外部传入的`Reporter`
* `CompositeSpanReporter` 组合模式实现，当存在多个`Reporter`时组合实现
* `BoundedAsyncReporter` 将`Span`进行转码，发送给Zipkin服务

因此我们关注下`BoundedAsyncReporter`的具体实现
### 重要成员
* `AtomicBoolean started, closed` 开关控制
* `BytesEncoder<S> encoder` 编码器，将`Span`或其列表转换成二进制数据
* `ByteBoundedQueue pending` 等待推送给Zipkin服务端的`Span`队列
* `Sender sender` 数据发送者实现
* `CountDownLatch close` 后台守护进程的控制
* `ReporterMetrics metrics` 指标，主要关注`Span`的传输指标，比如`Span`传递的个数，二进制数据统计等等

#### 1.开启flush线程
```java
void startFlusherThread() {
      BufferNextMessage<S> consumer =
          BufferNextMessage.create(encoder.encoding(), messageMaxBytes, messageTimeoutNanos);
      Thread flushThread = threadFactory.newThread(new Flusher<>(this, consumer));
      flushThread.setName("AsyncReporter{" + sender + "}");
      flushThread.setDaemon(true);
      flushThread.start();
}
```
开启后台**守护进程**,利用`CountDownLatch`进行开关控制，循环进行`flush`操作
```java
static final class Flusher<S> implements Runnable {
    static final Logger logger = Logger.getLogger(Flusher.class.getName());

    final BoundedAsyncReporter<S> result;
    final BufferNextMessage<S> consumer;

    @Override
    public void run() {
        try {
            while (!result.closed.get()) {
                result.flush(consumer);
            }
        } catch (RuntimeException | Error e) {
            logger.log(Level.WARNING, "Unexpected error flushing spans", e);
            throw e;
        } finally {
            int count = consumer.count();
            if (count > 0) {
                result.metrics.incrementSpansDropped(count);
                logger.warning("Dropped " + count + " spans due to AsyncReporter.close()");
            }
            result.close.countDown();
        }

    }
}
```

#### 2. Span入队
```java
@Override public void report(S next){
        if(next==null)throw new NullPointerException("span == null");
        // Lazy start so that reporters never used don't spawn threads
        if(started.compareAndSet(false,true))startFlusherThread();
        metrics.incrementSpans(1);
        int nextSizeInBytes=encoder.sizeInBytes(next);
        int messageSizeOfNextSpan=sender.messageSizeInBytes(nextSizeInBytes);
        metrics.incrementSpanBytes(nextSizeInBytes);
        if(closed.get()||
        // don't enqueue something larger than we can drain
        messageSizeOfNextSpan>messageMaxBytes||
        !pending.offer(next,nextSizeInBytes)){
        metrics.incrementSpansDropped(1);
        }
}
```
将`Span`压入pending队列，metrics增加指标计算

#### 3.flush队列
`zipkin2.reporter.AsyncReporter.BoundedAsyncReporter.flush(zipkin2.reporter.BufferNextMessage<S>)`
```java
void flush(BufferNextMessage<S> bundler) {
  //todo span数据转换成二进制
  try {
    //发送给zipkin服务端
    sender.sendSpans(nextMessage).execute();
  } catch (Throwable t) {
    //todo 异常处理，包括指标的计算
  }
}
```

可以看到发送数据依赖`Sender#sendSpans`，目前已有的实现包括
* `ActiveMQSender`
* `KafkaSender`
* `RabbitMQSender`
* `RestTemplateSender` Spring Cloud环境下特有，利用REST方式发送数据

可以看到Sender往往会利用消息中间件传递数据,在Spring Cloud环境下，也可以利用REST的方法进行数据传输
`org.springframework.cloud.sleuth.zipkin2.RestTemplateSender`
```java
void post(byte[] json) {
	HttpHeaders httpHeaders = new HttpHeaders();
	httpHeaders.setContentType(this.mediaType);
	RequestEntity<byte[]> requestEntity = new RequestEntity<>(json, httpHeaders, HttpMethod.POST,
			URI.create(this.url));
	this.restTemplate.exchange(requestEntity, String.class);
}
```

#### 内存占用限制
由于`Span`并不是同步发送，而是存储先在本地的pending队列，因此会占用一定的内存空间。Zipkin在此做了限制，默认情况下，`Span`在队列中的个数不会超过1000个，占用内存大小不会超过1%(当前JVM进程的占用内存)
```java
static int onePercentOfMemory() {
      long result = (long) (Runtime.getRuntime().totalMemory() * 0.01);
      // don't overflow in the rare case 1% of memory is larger than 2 GiB!
      return (int) Math.max(Math.min(Integer.MAX_VALUE, result), Integer.MIN_VALUE);
}

//zipkin2.reporter.ByteBoundedQueue.offer
@Override public boolean offer(S next, int nextSizeInBytes) {
        lock.lock();
        try {
            if (count == maxSize) return false;
            if (sizeInBytes + nextSizeInBytes > maxBytes) return false;

            elements[writePos] = next;
            sizesInBytes[writePos++] = nextSizeInBytes;

            if (writePos == maxSize) writePos = 0; // circle back to the front of the array

            count++;
            sizeInBytes += nextSizeInBytes;

            available.signal(); // alert any drainers
            return true;
            } finally {
                lock.unlock();
            }
        }
```

## 了解 Zipkin 如何实现分布式数据采集和存储
Zipkin服务端的数据库存储依赖[`zipkin-storage`](https://github.com/openzipkin/zipkin/tree/master/zipkin-storage)模块
主要核心接口如下:
### `SpanConsumer`
`Span`在服务端的消费者，即实际存储的操作者

### `SpanStore`
`Span`的数据访问仓库
```java
public interface SpanStore {

  Call<List<List<Span>>> getTraces(QueryRequest request);

  @Deprecated Call<List<Span>> getTrace(String traceId);

  @Deprecated Call<List<String>> getServiceNames();

  @Deprecated Call<List<String>> getSpanNames(String serviceName);

  Call<List<DependencyLink>> getDependencies(long endTs, long lookback);
}
```
目前官方默认实现有4种
* `ElasticsearchSpanStore` 基于Elasticsearch，即将文档数据存储在Elasticsearch
* `MySQLSpanStore ` 基于MYSQL
* `CassandraSpanStore` 基于Cassandra
* `InMemoryStorage` 基于内存，即直接将数据存储在内存之中



