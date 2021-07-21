package indi.kurok1.rest.converter;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * the collection of HttpBodyConverter
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.20
 */
public class HttpBodyConverters implements Iterable<HttpBodyConverter<?, ?>> {

   private final CopyOnWriteArrayList<HttpBodyConverter<?, ?>> loadedHttpBodyConverters = new CopyOnWriteArrayList<>();

    public HttpBodyConverters() {
        loadSpi();
    }

    public HttpBodyConverters(Collection<HttpBodyConverter<?, ?>> converters) {
        this();
        this.loadedHttpBodyConverters.addAll(converters);
    }

    private void loadSpi() {
        ServiceLoader<HttpBodyConverter> serviceLoader = ServiceLoader.load(HttpBodyConverter.class);
        Iterator<HttpBodyConverter> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            loadedHttpBodyConverters.add(iterator.next());
        }
    }

    public HttpBodyConverter<?, ?> getReadableConverter(Class<?> type, Type genericType,
                                                               Annotation[] annotations, MediaType mediaType) {
        if (loadedHttpBodyConverters.isEmpty())
            throw new IllegalStateException();

        List<HttpBodyConverter<?, ?>> returnConverters = new LinkedList<>();
        for (HttpBodyConverter<?, ?> converter : loadedHttpBodyConverters) {
            if (converter.isReadable(type, genericType, annotations, mediaType))
                return converter;
        }
        return getDefault();
    }

    public HttpBodyConverter<?, ?> getWriteableConverter(Class<?> type, Type genericType,
                                                        Annotation[] annotations, MediaType mediaType) {
        if (loadedHttpBodyConverters.isEmpty())
            throw new IllegalStateException();

        List<HttpBodyConverter<?, ?>> returnConverters = new LinkedList<>();
        for (HttpBodyConverter<?, ?> converter : loadedHttpBodyConverters) {
            if (converter.isWriteable(type, genericType, annotations, mediaType))
                return converter;
        }
        return getDefault();
    }

    public HttpBodyConverters add(int index, HttpBodyConverter<?, ?> converter) {
        this.loadedHttpBodyConverters.add(index, converter);
        return this;
    }

    public HttpBodyConverters addFirst(HttpBodyConverter<?, ?> converter) {
        return add(0, converter);
    }

    public HttpBodyConverters addLast(HttpBodyConverter<?, ?> converter) {
        return add(loadedHttpBodyConverters.size(), converter);
    }

    @Override
    public Iterator<HttpBodyConverter<?, ?>> iterator() {
        return loadedHttpBodyConverters.iterator();
    }

    @Override
    public void forEach(Consumer<? super HttpBodyConverter<?, ?>> action) {
        loadedHttpBodyConverters.forEach(action);
    }

    @Override
    public Spliterator<HttpBodyConverter<?, ?>> spliterator() {
        return loadedHttpBodyConverters.spliterator();
    }

    public HttpBodyConverter<?, ?> getDefault() {
        if (loadedHttpBodyConverters.isEmpty()) {
            loadedHttpBodyConverters.add(new JacksonJsonHttpBodyConverter());
        }
        return loadedHttpBodyConverters.get(0);
    }
}
