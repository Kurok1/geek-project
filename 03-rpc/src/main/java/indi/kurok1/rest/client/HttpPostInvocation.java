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
import java.util.concurrent.*;

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

    private HttpURLConnection connection = null;
    private HttpBodyConverter converter;

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

    @Override
    public Invocation property(String name, Object value) {
        properties.put(name, value);
        return this;
    }


    private void sendRequest() {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(HttpMethod.POST);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            setRequestHeaders(connection);
            writeEntity(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response invoke() {
        sendRequest();
        // TODO Set the cookies
        int statusCode = 0;
        try {
            statusCode = connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DefaultResponse response = new DefaultResponse();
        response.setConnection(connection);
        response.setStatus(statusCode);
        return response;
    }




    private void writeEntity(HttpURLConnection connection) throws IOException {
        if (entity != null && entity.getEntity() != null) {
            Class<?> clazz = entity.getClass();
            Type type = clazz.getGenericSuperclass();
            Annotation[] annotations = entity.getAnnotations();
            MediaType mediaType = entity.getMediaType();
            converter = converters.getWriteableConverter(clazz, type, annotations, mediaType);
            if (converter.isWriteable(clazz, type, annotations, mediaType)) {
                OutputStream outputStream = connection.getOutputStream();
                long length = converter.getSize(entity.getEntity(), clazz, type, annotations, mediaType);
                //connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
                converter.writeTo(entity.getEntity(), clazz, type, annotations, mediaType, headers, outputStream);
            }
        }
    }

    private void setRequestHeaders(HttpURLConnection connection) {
        this.properties.forEach(
                (key, value)->
                        connection.setRequestProperty(key.toString(), value.toString())
        );
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            for (Object headerValue : entry.getValue()) {
                connection.setRequestProperty(headerName, headerValue.toString());
            }
        }
    }

    @Override
    public <T> T invoke(Class<T> responseType) {
        sendRequest();
        // TODO Set the cookies
        int statusCode = 0;
        try {
            statusCode = connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DefaultResponse response = new DefaultResponse();
        response.setConnection(connection);
        response.setStatus(statusCode);
        response.setConverter(converter);
        response.readEntity(responseType);
        response.setAnnotations(entity.getAnnotations());
        response.setMediaType(entity.getMediaType());
        return (T) response.getEntity();
    }

    @Override
    public <T> T invoke(GenericType<T> responseType) {
        return null;
    }

    @Override
    public Future<Response> submit() {
        FutureTask<Response> futureTask = new FutureTask<>(this::invoke);
        executorService.submit(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Class<T> responseType) {
        FutureTask<T> futureTask = new FutureTask<>(new PostCallable<>(responseType, this));
        executorService.submit(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(GenericType<T> responseType) {
        return null;
    }

    @Override
    public <T> Future<T> submit(InvocationCallback<T> callback) {
        return null;
    }

    public static class PostCallable<V> implements Callable<V> {

        private final Class<V> responseType;

        private final HttpPostInvocation invocation;

        public PostCallable(Class<V> responseType, HttpPostInvocation invocation) {
            this.responseType = responseType;
            this.invocation = invocation;
        }

        @Override
        public V call() throws Exception {
            return invocation.invoke(responseType);
        }
    }

}
