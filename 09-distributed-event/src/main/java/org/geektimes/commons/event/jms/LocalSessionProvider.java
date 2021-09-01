package org.geektimes.commons.event.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Properties;

/**
 * {@link javax.jms.Session} provider.create with properties
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.09.01
 * @see Session
 * @see Connection
 */
public abstract class LocalSessionProvider {

    private Connection connection = null;

    public abstract Connection getOpenConnection(final Properties properties) throws JMSException;

    public final Session getSession(final Properties properties) throws JMSException {
        if (this.connection == null) {
            this.connection = getOpenConnection(properties);
        }

        return connection.createSession(isTransacted(), getAcknowledgeMode());
    }

    protected boolean isTransacted() {
        return false;
    }

    protected int getAcknowledgeMode() {
        return Session.AUTO_ACKNOWLEDGE;
    }

}
