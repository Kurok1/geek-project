package indi.kurok1.event.jms;

import indi.kurok1.event.jms.object.User;
import org.geektimes.commons.event.jms.*;
import org.geektimes.commons.event.jms.publisher.JmsEventPublisher;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

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

        User user = new User();
        user.setId(1);
        user.setName("user");
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new ObjectJmsEvent(user));

        Map<String, Object> map = Collections.singletonMap("key", "value");
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new MapJmsEvent(map));

        byte[] bytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        jmsEventPublisher.publish(Destination.fromActiveMQ(), new BytesJmsEvent(bytes));
    }

}
