package org.geektimes.commons.event.jms;

/**
 * topic
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.08.31
 */
public final class Destination {

    private final String name;

    private Destination(String name) {
        this.name = name;
    }

    private final static Destination ACTIVEMQ = fromName("jms:activemq");

    public static Destination fromName(String name) {
        return new Destination(name);
    }

    public static Destination fromActiveMQ() {
        return ACTIVEMQ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Destination that = (Destination) o;

        return name.equals(that.name);
    }
}
