package indi.kurok1.event.jms.object;

import org.geektimes.commons.event.EventListener;
import org.geektimes.commons.event.jms.ObjectJmsEvent;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.09.01
 */
public class UserEventListener implements EventListener<ObjectJmsEvent> {

    @Override
    public void onEvent(ObjectJmsEvent event) {
        System.out.println(String.format("received user event : [{%s}]", event.getSource().toString()));
    }
}
