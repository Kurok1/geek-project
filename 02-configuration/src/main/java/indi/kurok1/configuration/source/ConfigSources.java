package indi.kurok1.configuration.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.14
 */
public class ConfigSources implements Iterable<ConfigSource> {

    private final List<ConfigSource> sourceList = new CopyOnWriteArrayList<>();

    @Override
    public Iterator<ConfigSource> iterator() {
        return sourceList.iterator();
    }

    @Override
    public void forEach(Consumer<? super ConfigSource> action) {
        sourceList.forEach(action);
    }

    @Override
    public Spliterator<ConfigSource> spliterator() {
        return sourceList.spliterator();
    }

    public void addFirst(ConfigSource configSource) {
        this.sourceList.add(0, configSource);
    }

    public void addLast(ConfigSource configSource) {
        this.sourceList.add(this.sourceList.size(), configSource);
    }

    public void add(int position, ConfigSource configSource) {
        this.sourceList.add(position, configSource);
    }

    public void with(Collection<ConfigSource> configSources) {
        this.sourceList.addAll(configSources);
    }

    public void release(String name) {
        if (name == null)
            throw new NullPointerException();

        Iterator<ConfigSource> iterator = iterator();
        while (iterator.hasNext()) {
            ConfigSource configSource = iterator().next();
            if (configSource.getName().equals(name)) {
                iterator.remove();
                break;
            }

        }
    }

}
