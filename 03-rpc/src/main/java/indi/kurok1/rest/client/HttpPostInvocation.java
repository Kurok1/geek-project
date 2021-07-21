package indi.kurok1.rest.client;

import indi.kurok1.rest.converter.HttpBodyConverter;
import indi.kurok1.rest.converter.HttpBodyConverters;
import indi.kurok1.rest.core.DefaultResponse;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * HTTP post实现
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.20
 * @see Invocation
 */
public class HttpPostInvocation implements Invocation {


    private final HttpBodyConverters converters = new HttpBodyConverters();

    private final MultivaluedMap<String, Object> headers;

    private final URL url;
    private final URI uri;

    private Entity<?> entity = null;
    private Properties properties = new Properties();

    public HttpPostInvocation(MultivaluedMap<String, Object> headers, URI uri) {
        this.headers = headers;
        this.uri = uri;
        try {
            this.url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public HttpPostInvocation(MultivaluedMap<String, Object> headers, URL url) {
        this.headers = headers;
        this.url = url;
        try {
            this.uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException();
        }
    }

    public HttpPostInvocation(MultivaluedMap<String, Object> headers, URI uri, Entity<?> entity) {
        this(headers, uri);
        this.entity = entity;
    }

    public HttpPostInvocation(MultivaluedMap<String, Object> headers, URL url, Entity<?> entity) {
        this(headers, url);
        this.entity = entity;
    }

    private HttpHeaders toHttpHeaders() {
        Locale locale = entity.getLanguage();
        MediaType mediaType = entity.getMediaType();
        return new DefaultHttpHeaders(headers, mediaType, locale);
    }

    @Override
    public Invocation property(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public Response invoke() {
        HttpURLConnection connection = null;
        try {
            InputStream inputStream = url.openStream();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(HttpMethod.POST);
            setRequestHeaders(connection);
            writeEntity(connection);
            // TODO Set the cookies
            int statusCode = connection.getResponseCode();
//            Response.ResponseBuilder responseBuilder = Response.status(statusCode);
//
//            responseBuilder.build();
            DefaultResponse response = new DefaultResponse();
            response.setConnection(connection);
            response.setStatus(statusCode);
            return response;
//            Response.Status status = Response.Status.fromStatusCode(statusCode);
//            switch (status) {
//                case Response.Status.OK:
//
//                    break;
//                default:
//                    break;
//            }

        } catch (IOException e) {
            // TODO Error handler
        }
        return null;
    }


    private void writeEntity(HttpURLConnection connection) throws IOException {
        if (entity != null && entity.getEntity() != null) {
            Class<?> clazz = entity.getClass();
            Type type = clazz.getGenericSuperclass();
            Annotation[] annotations = entity.getAnnotations();
            MediaType mediaType = entity.getMediaType();
            HttpBodyConverter converter = converters.getWriteableConverter(clazz, type, annotations, mediaType);
            if (converter.isWriteable(clazz, type, annotations, mediaType)) {
                OutputStream outputStream = connection.getOutputStream();
                long length = converter.getSize(entity.getEntity(), clazz, type, annotations, mediaType);
                connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
                converter.writeTo(entity.getEntity(), clazz, type, annotations, mediaType, headers, outputStream);
            }
        }
    }

    private void setRequestHeaders(HttpURLConnection connection) {
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            for (Object headerValue : entry.getValue()) {
                connection.setRequestProperty(headerName, headerValue.toString());
            }
        }
    }

    @Override
    public <T> T invoke(Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T invoke(GenericType<T> responseType) {
        return null;
    }

    @Override
    public Future<Response> submit() {
        return null;
    }

    @Override
    public <T> Future<T> submit(Class<T> responseType) {
        return null;
    }

    @Override
    public <T> Future<T> submit(GenericType<T> responseType) {
        return null;
    }

    @Override
    public <T> Future<T> submit(InvocationCallback<T> callback) {
        return null;
    }

}
