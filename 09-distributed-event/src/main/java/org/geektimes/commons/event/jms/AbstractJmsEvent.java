package org.geektimes.commons.event.jms;

import org.geektimes.commons.event.GenericEvent;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.net.URI;

/**
 * event based on JMS
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public abstract class AbstractJmsEvent<S> extends GenericEvent<S> {

    public static final String topic = "distribute-event-topic";

    public AbstractJmsEvent(S source) {
        super(source);
    }

    public static final String EVENT_CREATED_TIMESTAMP_PROPERTY = "event.created.timestamp";
    public static final String EVENT_TYPE_PROPERTY = "event.class.type";
    public static final String MESSAGE_TYPE_PROPERTY = "message.class.type";

    public abstract Message createMessage(Session session) throws JMSException;

    protected <M extends Message> M setBaseProperties(Message message) throws JMSException {
        message.setLongProperty(EVENT_CREATED_TIMESTAMP_PROPERTY, getTimestamp());
        message.setStringProperty(EVENT_TYPE_PROPERTY, getClass().getName());
        message.setStringProperty(MESSAGE_TYPE_PROPERTY, getMessageClassType().getName());
        return (M) message;
    }

    protected abstract Class<?> getMessageClassType();
}
