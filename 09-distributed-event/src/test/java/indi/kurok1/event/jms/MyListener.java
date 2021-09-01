package indi.kurok1.event.jms;

import org.geektimes.commons.event.EventListener;
import org.geektimes.commons.event.jms.TextJmsEvent;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.09.01
 */
public class MyListener implements EventListener<TextJmsEvent> {

    @Override
    public void onEvent(TextJmsEvent event) {
        System.out.println(event.getSource());
    }
}
