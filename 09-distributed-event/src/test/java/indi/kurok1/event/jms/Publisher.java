package indi.kurok1.event.jms;

import org.geektimes.commons.event.jms.Destination;
import org.geektimes.commons.event.jms.TextJmsEvent;
import org.geektimes.commons.event.jms.publisher.JmsEventPublisher;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.09.01
 */
public class Publisher{

    public static void main(String[] args) {
        JmsEventPublisher jmsEventPublisher = new JmsEventPublisher();
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new TextJmsEvent("Hello"));
    }

}
