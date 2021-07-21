package indi.kurok1.rest.converter;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.21
 * @see HttpBodyConverter
 */
public class StringHttpBodyConverter implements HttpBodyConverter<String, String> {

    private final List<MediaType> supportedMediaTypes = Arrays.asList(MediaType.TEXT_PLAIN_TYPE);

    private Charset charset = StandardCharsets.UTF_8;

    public StringHttpBodyConverter() {
    }

    public StringHttpBodyConverter(Charset charset) {
        this.charset = charset;
    }

    public void addSupportedMediaType(MediaType mediaType) {
        supportedMediaTypes.add(mediaType);
    }

    public void addSupportedMediaTypes(MediaType... mediaType) {
        supportedMediaTypes.addAll(Arrays.asList(mediaType));
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type != String.class)
            return false;

        if (mediaType == null) {
            return false;
        }
        for (MediaType supportedMediaType : supportedMediaTypes) {
            if (supportedMediaType.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String readFrom(Class<String> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return IOUtils.toString(entityStream);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type != String.class)
            return false;

        if (mediaType == null) {
            return false;
        }
        for (MediaType supportedMediaType : supportedMediaTypes) {
            if (supportedMediaType.isCompatible(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSize(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return s.length();
    }

    @Override
    public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(s.getBytes(charset));
        entityStream.flush();
    }
}
