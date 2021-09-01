package org.geektimes.commons.event.jms.subscriber;

import org.geektimes.commons.event.ConditionalEventListener;
import org.geektimes.commons.event.Event;
import org.geektimes.commons.event.EventDispatcher;
import org.geektimes.commons.event.EventListener;
import org.geektimes.commons.event.jms.AbstractJmsEvent;
import org.geektimes.commons.event.jms.LocalSessionProvider;
import org.geektimes.commons.event.jms.publisher.JmsEventEmitter;

import javax.jms.*;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * the subscriber for jms event
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public abstract class JmsEventSubscriber extends LocalSessionProvider implements MessageListener, EventDispatcher {

    private final CopyOnWriteArrayList<EventListener<? extends Event>> listeners = new CopyOnWriteArrayList<>();

    protected final Properties properties;

    public JmsEventSubscriber() {
        this.properties = loadProperties();
        loadListenersFromSpi();
    }

    private final MessageEventResolver resolver = new MessageEventResolver();

    protected void loadListenersFromSpi() {
        ServiceLoader.load(EventListener.class).forEach(this::addEventListener);
    }

    @Override
    public void onMessage(Message message) {
        try {
            dispatch(resolver.resolveMessage(message));
        } catch (JMSException e) {
            //不要终止进程
            System.err.println(e.getMessage());
        }
    }

    protected Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/jms.properties"));
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return properties;
    }

    protected Destination getDestination() {
        return (Topic) () -> AbstractJmsEvent.topic;
    }

    @Override
    public void dispatch(Event event) {
        Executor executor = getExecutor();

        // execute in sequential or parallel execution model
        executor.execute(() -> {
            for (EventListener listener : listeners) {
                Class<? extends Event> eventType = EventListener.findEventType(listener);
                //判断类型
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

    @Override
    public void addEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException {
        if (listener == null)
            throw new NullPointerException();
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(EventListener<?> listener) throws NullPointerException, IllegalArgumentException {
        if (listener == null)
            throw new NullPointerException();
        listeners.remove(listener);
    }

    @Override
    public List<EventListener<?>> getAllEventListeners() {
        return listeners;
    }

    protected void onDestroy() {
        this.listeners.clear();
    }


}
