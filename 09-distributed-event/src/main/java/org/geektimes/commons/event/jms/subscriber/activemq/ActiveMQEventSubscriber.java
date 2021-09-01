package org.geektimes.commons.event.jms.subscriber.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.geektimes.commons.event.jms.subscriber.JmsEventSubscriber;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * {@link JmsEventSubscriber} implements of activemq
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.09.01
 * @see JmsEventSubscriber
 */
public class ActiveMQEventSubscriber extends JmsEventSubscriber {

    private final static String HOST_PROPERTIES = "jms.activemq.endpoint.host";
    private final static String PORT_PROPERTIES = "jms.activemq.endpoint.port";


    protected URI buildRealEndpointURI(final Properties properties) throws URISyntaxException {
        String host = properties.getProperty(HOST_PROPERTIES, "localhost");
        int port = -1;
        try {
            port = Integer.parseInt(properties.getProperty(PORT_PROPERTIES, "0"));
        } catch (NumberFormatException ignored) {
        }
        if (port <= 0)
            port = 61616;
        return new URI(String.format("tcp://%s:%d", host, port));
    }

    @Override
    public Connection getOpenConnection(Properties properties) throws JMSException {
        ActiveMQConnectionFactory factory = null;
        try {
            factory = new ActiveMQConnectionFactory(buildRealEndpointURI(properties));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        ActiveMQConnection conn = (ActiveMQConnection) factory.createConnection();
        conn.start();
        return conn;
    }
}
