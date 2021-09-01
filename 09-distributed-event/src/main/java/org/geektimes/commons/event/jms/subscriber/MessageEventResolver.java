package org.geektimes.commons.event.jms.subscriber;

import org.geektimes.commons.event.jms.*;

import javax.jms.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public class MessageEventResolver {

    public AbstractJmsEvent resolveMessage(Message message) throws JMSException {
        if (isBytesMessage(message))
            return resolveToBytesEvent((BytesMessage) message);

        if (isTextMessage(message))
            return resolveToTextEvent((TextMessage) message);

        if (isMapMessage(message))
            return resolveToMapEvent((MapMessage) message);

        if (isObjectMessage(message))
            return resolveToObjectEvent((ObjectMessage) message);

        throw new UnsupportedOperationException("no message type supported");
    }

    protected TextJmsEvent resolveToTextEvent(TextMessage message) throws JMSException {
        return new TextJmsEvent(message.getText());
    }

    protected ObjectJmsEvent resolveToObjectEvent(ObjectMessage message) throws JMSException {
        return new ObjectJmsEvent(message.getObject());
    }

    protected BytesJmsEvent resolveToBytesEvent(BytesMessage message) throws JMSException {
        byte[] bytes = new byte[new Long(message.getBodyLength()).intValue()];
        message.readBytes(bytes);
        return new BytesJmsEvent(bytes);
    }

    protected MapJmsEvent resolveToMapEvent(MapMessage message) throws JMSException {
        Map<String, Object> map = new HashMap<>();
        Enumeration mapNames = message.getMapNames();
        while (mapNames.hasMoreElements()) {
            String key = mapNames.nextElement().toString();
            map.put(key, message.getObject(key));
        }
        return new MapJmsEvent(map);
    }


    private boolean isTextMessage(Message message) throws JMSException {
        return TextMessage.class.getName().equals(message.getStringProperty(AbstractJmsEvent.MESSAGE_TYPE_PROPERTY));
    }

    private boolean isMapMessage(Message message) throws JMSException {
        return MapMessage.class.getName().equals(message.getStringProperty(AbstractJmsEvent.MESSAGE_TYPE_PROPERTY));
    }

    private boolean isBytesMessage(Message message) throws JMSException {
        return BytesMessage.class.getName().equals(message.getStringProperty(AbstractJmsEvent.MESSAGE_TYPE_PROPERTY));
    }

    private boolean isObjectMessage(Message message) throws JMSException {
        return ObjectMessage.class.getName().equals(message.getStringProperty(AbstractJmsEvent.MESSAGE_TYPE_PROPERTY));
    }

}
