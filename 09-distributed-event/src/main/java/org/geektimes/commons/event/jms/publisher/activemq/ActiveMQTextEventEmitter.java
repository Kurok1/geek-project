package org.geektimes.commons.event.jms.publisher.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.geektimes.commons.event.jms.Destination;
import org.geektimes.commons.event.jms.TextJmsEvent;
import org.geektimes.commons.event.jms.publisher.JmsEventEmitter;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * {@link JmsEventEmitter} implements of activemq
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 * @see JmsEventEmitter
 */
public final class ActiveMQTextEventEmitter extends ActiveMQEventEmitter<TextJmsEvent> {


}
