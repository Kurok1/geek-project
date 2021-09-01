package org.geektimes.commons.event.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public class BytesJmsEvent extends AbstractJmsEvent<byte[]> {

    public BytesJmsEvent(byte[] source) {
        super(source);
    }

    @Override
    public BytesMessage createMessage(Session session) throws JMSException {
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(getSource());
        return setBaseProperties(bytesMessage);
    }

    @Override
    protected Class<?> getMessageClassType() {
        return BytesMessage.class;
    }
}
