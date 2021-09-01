package org.geektimes.commons.event.jms;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;

/**
 * event based on {@link javax.jms.ObjectMessage}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public class ObjectJmsEvent extends AbstractJmsEvent<Serializable> {

    public ObjectJmsEvent(Serializable source) {
        super(source);
    }

    @Override
    public ObjectMessage createMessage(Session session) throws JMSException {
        return setBaseProperties(session.createObjectMessage(getSource()));
    }

    @Override
    protected Class<?> getMessageClassType() {
        return ObjectMessage.class;
    }
}
