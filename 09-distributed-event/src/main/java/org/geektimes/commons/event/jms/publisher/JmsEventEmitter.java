package org.geektimes.commons.event.jms.publisher;

import org.geektimes.commons.event.ConditionalEventListener;
import org.geektimes.commons.event.jms.AbstractJmsEvent;
import org.geektimes.commons.event.jms.LocalSessionProvider;
import org.geektimes.commons.event.jms.publisher.activemq.ActiveMQTextEventEmitter;

import javax.jms.*;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;


/**
 * generic jms event emitter
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 * @see ConditionalEventListener
 * @see ActiveMQTextEventEmitter
 */
public abstract class JmsEventEmitter<E extends AbstractJmsEvent<?>> extends LocalSessionProvider implements ConditionalEventListener<E> {

    protected final Properties properties = loadProperties();

    protected Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/jms.properties"));
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return properties;
    }

    @Override
    public boolean accept(E event) {
        return true;
    }

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
