## 实现分布式事件，基于 Zookeeper 或者 JMS 来实现

### 基于JMS消息的事件封装
抽象封装[AbstractJmsEvent](./src/main/java/org/geektimes/commons/event/jms/AbstractJmsEvent.java)
```java
public abstract class AbstractJmsEvent<S> extends GenericEvent<S> {

    public static final String topic = "distribute-event-topic";

    public AbstractJmsEvent(S source) {
        super(source);
    }
    
    public abstract Message createMessage(Session session) throws JMSException;

    protected <M extends Message> M setBaseProperties(Message message) throws JMSException;

    protected abstract Class<?> getMessageClassType();
}
```
* `createMessage` 根据当前事件，创建相应的`javax.jms.Message`
* `setBaseProperties` 酌情设置`javax.jms.Message`的属性，即消息头
* `getMessageClassType` 创建出来的真实`javax.jms.Message`类型

提供下列默认实现
* [TextJmsEvent](./src/main/java/org/geektimes/commons/event/jms/TextJmsEvent.java) 对应`javax.jms.TextMessage` 
* [MapJmsEvent](./src/main/java/org/geektimes/commons/event/jms/MapJmsEvent.java) 对应`javax.jms.MapMessage` 
* [ObjectJmsEvent](./src/main/java/org/geektimes/commons/event/jms/ObjectJmsEvent.java) 对应`javax.jms.ObjextMessage` 
* [BytesJmsEvent](./src/main/java/org/geektimes/commons/event/jms/BytesJmsEvent.java) 对应`javax.jms.BytesMessage` 

### 本地事件发送
#### 1.本地事件发布
[JmsEventPublisher](./src/main/java/org/geektimes/commons/event/jms/publisher/JmsEventPublisher.java)
```java
public class JmsEventPublisher extends ParallelEventDispatcher {

    public JmsEventPublisher() {
        super();
    }
    public JmsEventPublisher(Executor executor) {
        super(executor);
    }

    @Override
    protected void loadEventListenerInstances();

    public void publish(Destination destination, AbstractJmsEvent<?> event);
}
```
[Destination](./src/main/java/org/geektimes/commons/event/jms/Destination.java) 接口定义了目标JMS服务设施，即这个事件需要发送到哪个JMS上

`loadEventListenerInstances`用于加载已实现的本地事件发射器
#### 2.JMS消息推送
本地事件发射器抽象 [JmsEventEmitter](./src/main/java/org/geektimes/commons/event/jms/publisher/JmsEventEmitter.java),主要作用是创建本地`javax.jms.Session`并将事件转换成`javax.jms.Message`,根据`Destination`发送给JMS服务

```java
public abstract class JmsEventEmitter<E extends AbstractJmsEvent<?>> extends LocalSessionProvider implements ConditionalEventListener<E> {

    protected final Properties properties = loadProperties();

    /**
     * @return 从配置文件加载JMS连接相关配置
     */
    protected Properties loadProperties();

    @Override
    public boolean accept(E event);

    @Override
    public void onEvent(E event) {
        try {
            Session session = getSession(this.properties);
            MessageProducer producer = session.createProducer(getDestination());
            Message message = event.createMessage(session);
            producer.send(message);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Set<org.geektimes.commons.event.jms.Destination> getSupportedDestinations();

    protected Destination getDestination() {
        return (Topic) () -> AbstractJmsEvent.topic;
    }

}
```

目前实现：
* 基于ActiveMQ, [ActiveMQEventEmitter](./src/main/java/org/geektimes/commons/event/jms/publisher/activemq/ActiveMQEventEmitter.java)

### 本地事件接收
#### 1.JMS消息订阅
定义抽象JMS消息订阅 [JmsEventSubscriber](./src/main/java/org/geektimes/commons/event/jms/subscriber/JmsEventSubscriber.java)
```java
public abstract class JmsEventSubscriber extends LocalSessionProvider implements MessageListener, EventDispatcher, Runnable {

    private final CopyOnWriteArrayList<EventListener<? extends Event>> listeners = new CopyOnWriteArrayList<>();
    protected final Properties properties;
    private final MessageEventResolver resolver = new MessageEventResolver();

    public JmsEventSubscriber() {
        this.properties = loadProperties();
        loadListenersFromSpi();
    }
    protected void loadListenersFromSpi();

    @Override
    public void onMessage(Message message) {
        try {
            dispatch(resolver.resolveMessage(message));
        } catch (JMSException e) {
            //不要终止进程
            System.err.println(e.getMessage());
        }
    }
    protected Properties loadProperties();
    protected Destination getDestination() {
        return (Topic) () -> AbstractJmsEvent.topic;
    }

    /**
     * 分发事件给本地
     * @param event
     */
    @Override
    public void dispatch(Event event);
    @Override
    public void addEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException;
    @Override
    public void removeEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException;
    @Override
    public List<EventListener<?>> getAllEventListeners();

    @Override
    public void run() {//监听
        try {
            MessageConsumer consumer = getSession(this.properties).createConsumer(this.getDestination());
            consumer.setMessageListener(this);
            while (true) {
                //阻塞当前线程
            }
        } catch (Throwable t) {
            onDestroy();
            throw new RuntimeException(t);
        }
    }
    /**
     * 订阅线程出现异常时触发
     */
    protected void onDestroy();
}
```
其中[MessageEventResolver](./src/main/java/org/geektimes/commons/event/jms/subscriber/MessageEventResolver.java) 用于将订阅得到的`javax.jms.Message`转换成本地事件

目前实现
* ActiveMQ [ActiveMQEventSubscriber](./src/main/java/org/geektimes/commons/event/jms/subscriber/activemq/ActiveMQEventSubscriber.java)

#### 2.本地消息推送
本地事件推送的核心处理定义在`org.geektimes.commons.event.jms.subscriber.JmsEventSubscriber#dispatch(Event)`方法中
```java
    public void dispatch(Event event) {
        Executor executor = getExecutor();

        // execute in sequential or parallel execution model
        executor.execute(() -> {
            for (EventListener listener : listeners) {
                Class<? extends Event> eventType = EventListener.findEventType(listener);
                //判断类型一致
                if (!event.getClass().equals(eventType))
                    continue;

                if (listener instanceof ConditionalEventListener) {
                    ConditionalEventListener predicateEventListener = (ConditionalEventListener) listener;
                    if (!predicateEventListener.accept(event)) { // No accept
                        return;
                }
            }
            // Handle the event
            listener.onEvent(event);
            }
        });
    }
```
根据已注册的`EventListener`,判断监听事件类型，完成分发

### 代码测试
4种事件的监听
```java
public class BytesListener implements EventListener<BytesJmsEvent> {

    @Override
    public void onEvent(BytesJmsEvent event) {
        System.out.println(new String(event.getSource()));
    }
}

public class MapListener implements EventListener<MapJmsEvent> {

    @Override
    public void onEvent(MapJmsEvent event) {
        System.out.println(event.getSource());
    }
}

public class MyListener implements EventListener<TextJmsEvent> {

    @Override
    public void onEvent(TextJmsEvent event) {
        System.out.println(event.getSource());
    }
}

public class UserEventListener implements EventListener<ObjectJmsEvent> {

    @Override
    public void onEvent(ObjectJmsEvent event) {
        System.out.println(String.format("received user event : [{%s}]", event.getSource().toString()));
    }
}
```

订阅
```java
public class Subscriber {

    public static void main(String[] args) {
        new Thread(new ActiveMQEventSubscriber()).start();
    }

}
```

发布事件
```java
public class Publisher{

    public static void main(String[] args) {
        JmsEventPublisher jmsEventPublisher = new JmsEventPublisher();
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new TextJmsEvent("Hello"));

        User user = new User();
        user.setId(1);
        user.setName("user");
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new ObjectJmsEvent(user));

        Map<String, Object> map = Collections.singletonMap("key", "value");
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new MapJmsEvent(map));

        byte[] bytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new BytesJmsEvent(bytes));
    }

}
```

测试输出
```text
Hello
received user event : [{User{id=1, name='user'}}]
{key=value}
Hello, World!
```