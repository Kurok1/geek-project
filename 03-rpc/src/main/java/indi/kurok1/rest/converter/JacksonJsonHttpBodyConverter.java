package indi.kurok1.rest.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.21
 */
public class JacksonJsonHttpBodyConverter implements HttpBodyConverter<Object, Object> {

    private final MediaType all = new MediaType("application", "*+json");

    private final List<MediaType> supportedMediaTypes = Arrays.asList(MediaType.APPLICATION_JSON_TYPE, all);

    private final ObjectMapper mapper = new ObjectMapper();

    public void addSupportedMediaType(MediaType mediaType) {
        supportedMediaTypes.add(mediaType);
    }

    public void addSupportedMediaTypes(MediaType... mediaType) {
        supportedMediaTypes.addAll(Arrays.asList(mediaType));
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        for (MediaType supportedMediaType : supportedMediaTypes) {
            if (supportedMediaType.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return null;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        for (MediaType supportedMediaType : supportedMediaTypes) {
            if (supportedMediaType.isCompatible(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

    }
}
