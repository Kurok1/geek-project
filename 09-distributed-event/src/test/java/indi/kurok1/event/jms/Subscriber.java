package indi.kurok1.event.jms;

import org.geektimes.commons.event.jms.subscriber.activemq.ActiveMQEventSubscriber;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.09.01
 */
public class Subscriber {

    public static void main(String[] args) {
        new Thread(new ActiveMQEventSubscriber()).start();
    }

}
