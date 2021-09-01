package org.geektimes.commons.event.jms;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import java.util.Map;

/**
 * Event based on {@link MapMessage}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 * @see MapMessage
 */
public class MapJmsEvent extends AbstractJmsEvent<Map<String, Object>> {

    public MapJmsEvent(Map<String, Object> source) {
        super(source);
    }

    @Override
    public MapMessage createMessage(Session session) throws JMSException {
        MapMessage message = session.createMapMessage();
        for (Map.Entry<String, Object> entry : getSource().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            message.setObject(key, value);
        }
        return setBaseProperties(message);
    }

    @Override
    protected Class<?> getMessageClassType() {
        return MapMessage.class;
    }
}
