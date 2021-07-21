package indi.kurok1.rest.converter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
        JavaType javaType = getJavaType(type, genericType);
        return mapper.readValue(entityStream, javaType);
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

    private ByteBuffer byteBuffer = null;
    private void writeInternal(Object o, Class<?> type, Type genericType) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(o);
            byteBuffer = ByteBuffer.wrap(bytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        writeInternal(o, type, genericType);
        if (byteBuffer == null)
            return 0;
        else return byteBuffer.capacity();
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (byteBuffer != null) {
            entityStream.write(byteBuffer.array());
            byteBuffer = null;
            entityStream.flush();
        }
    }

    public JavaType getJavaType(Class<?> type, Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            //不携带泛型
            return mapper.getTypeFactory().constructType(type);
        } else {
            return resolveType(type, genericType);
        }
    }

    protected JavaType resolveType(Class<?> realClazz, Type genericType) {
        Type[] actualTypes = getActualTypeArguments(genericType);
        if (actualTypes.length == 0)
            return mapper.getTypeFactory().constructType(realClazz);
        JavaType[] javaTypes = new JavaType[actualTypes.length];
        int index = 0;
        for (Type type : actualTypes) {
            if (type instanceof ParameterizedType) {
                javaTypes[index] = resolveType(realClazz, type);//递归解析
            } else {
                javaTypes[index] = mapper.getTypeFactory().constructType(type);
            }
            index++;
        }
        return mapper.getTypeFactory().constructParametricType(realClazz, javaTypes);
    }

    protected Type[] getActualTypeArguments(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            return ((ParameterizedType) genericType).getActualTypeArguments();
        }
        //不是泛型，返回空数组
        return new Type[0];
    }

}
