package org.geektimes.commons.event.jms.publisher;

import org.geektimes.commons.event.ConditionalEventListener;
import org.geektimes.commons.event.ParallelEventDispatcher;
import org.geektimes.commons.event.jms.AbstractJmsEvent;
import org.geektimes.commons.event.jms.Destination;

import java.util.ServiceLoader;
import java.util.concurrent.Executor;

/**
 * TODO
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public class JmsEventPublisher extends ParallelEventDispatcher {

    public JmsEventPublisher() {
        super();
    }

    public JmsEventPublisher(Executor executor) {
        super(executor);
    }


    @Override
    protected void loadEventListenerInstances() {
        //load from spi
        ServiceLoader.load(JmsEventEmitter.class).forEach(this::addEventListener);
    }

    public void publish(Destination destination, AbstractJmsEvent<?> event) {
        Executor executor = getExecutor();

        // execute in sequential or parallel execution model

            sortedListeners(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                    .forEach(listener -> {
                        if (listener instanceof JmsEventEmitter) {
                            JmsEventEmitter jmsEventEmitter = (JmsEventEmitter) listener;
                            if (!jmsEventEmitter.getSupportedDestinations().contains(destination)) { // No accept
                                return;
                            }
                        }

                        if (listener instanceof ConditionalEventListener) {
                            ConditionalEventListener predicateEventListener = (ConditionalEventListener) listener;
                            if (!predicateEventListener.accept(event)) { // No accept
                                return;
                            }
                        }
                        // Handle the event
                        listener.onEvent(event);
                    });

    }


}
