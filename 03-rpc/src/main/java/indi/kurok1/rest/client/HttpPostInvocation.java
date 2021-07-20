package indi.kurok1.rest.client;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
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
        return null;
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
