package indi.kurok1.rest.client;

import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 默认实现 {@link HttpHeaders}
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.20
 */
public class DefaultHttpHeaders implements HttpHeaders {

    private final MultivaluedMap<String, Object> sources;
    private Locale locale = Locale.CHINA;
    private int length = 0;
    private MediaType mediaType;

    public DefaultHttpHeaders(MultivaluedMap<String, Object> sources, MediaType mediaType) {
        if (sources == null)
            throw new NullPointerException();
        this.sources = sources;
        this.mediaType = mediaType;
    }

    public DefaultHttpHeaders(MultivaluedMap<String, Object> sources, MediaType mediaType, Locale locale) {
        this(sources, mediaType);
        this.locale = locale;
    }

    public DefaultHttpHeaders(MultivaluedMap<String, Object> sources, MediaType mediaType, int contentLength) {
        this(sources, mediaType);
        this.length = contentLength;
    }

    public DefaultHttpHeaders(MultivaluedMap<String, Object> sources, MediaType mediaType, Locale locale, int length) {
        this(sources, mediaType);
        this.locale = locale;
        this.length = length;
    }

    @Override
    public List<String> getRequestHeader(String name) {
        List<String> list = new ArrayList<>(sources.size());
        list.addAll(sources.keySet());
        return Collections.unmodifiableList(list);
    }

    @Override
    public String getHeaderString(String name) {
        Object target = sources.get(name);
        return target == null ? null : target.toString();
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        final MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        sources.forEach(
                (key,value)->{
                    header.add(key, value.toString());
                }
        );
        return header;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        //todo more...
        return Arrays.asList(
                MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_XML_TYPE,
                MediaType.APPLICATION_XHTML_XML_TYPE,
                MediaType.TEXT_HTML_TYPE,
                MediaType.TEXT_PLAIN_TYPE
        );
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return Arrays.asList(Locale.CHINA, Locale.ENGLISH, Locale.US, Locale.UK);
    }

    @Override
    public MediaType getMediaType() {
        return this.mediaType;
    }

    @Override
    public Locale getLanguage() {
        return this.locale == null ? Locale.CHINA : this.locale;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return Collections.emptyMap();
    }

    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public int getLength() {
        return this.length;
    }
}
