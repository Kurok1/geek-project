package org.geektimes.commons.event.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Event based on {@link TextMessage}
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 * @see TextMessage
 */
public final class TextJmsEvent extends AbstractJmsEvent<String> {


    public TextJmsEvent(String source) {
        super(source);
    }

    @Override
    public TextMessage createMessage(Session session) throws JMSException {
        return setBaseProperties(session.createTextMessage(getSource()));
    }

    @Override
    protected Class<?> getMessageClassType() {
        return TextMessage.class;
    }
}
