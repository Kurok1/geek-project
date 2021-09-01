package indi.kurok1.event.jms;

import org.geektimes.commons.event.EventListener;
import org.geektimes.commons.event.jms.BytesJmsEvent;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.09.01
 */
public class BytesListener implements EventListener<BytesJmsEvent> {

    @Override
    public void onEvent(BytesJmsEvent event) {
        System.out.println(new String(event.getSource()));
    }
}
