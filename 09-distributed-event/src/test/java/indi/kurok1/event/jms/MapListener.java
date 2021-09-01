package indi.kurok1.event.jms;

import org.geektimes.commons.event.EventListener;
import org.geektimes.commons.event.jms.MapJmsEvent;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.09.01
 */
public class MapListener implements EventListener<MapJmsEvent> {

    @Override
    public void onEvent(MapJmsEvent event) {
        System.out.println(event.getSource());
    }
}
