package indi.kurok1.rest;

import indi.kurok1.rest.core.DefaultResponseBuilder;
import indi.kurok1.rest.core.DefaultUriBuilder;
import indi.kurok1.rest.client.DefaultVariantListBuilder;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.RuntimeDelegate;

public class DefaultRuntimeDelegate extends RuntimeDelegate {

    @Override
    public UriBuilder createUriBuilder() {
        return new DefaultUriBuilder();
    }

    @Override
    public Response.ResponseBuilder createResponseBuilder() {
        return new DefaultResponseBuilder();
    }

    @Override
    public Variant.VariantListBuilder createVariantListBuilder() {
        return new DefaultVariantListBuilder();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType) throws IllegalArgumentException, UnsupportedOperationException {
        return null;
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
        if (type == MediaType.class) {
            return (HeaderDelegate<T>) new HeaderDelegate<MediaType>() {
                @Override
                public MediaType fromString(String value) {
                    String[] types = value.split("/");
                    return new MediaType(types[0], types[1]);
                }

                @Override
                public String toString(MediaType value) {
                    return String.format("%s/%s", value.getType(), value.getSubtype());
                }
            };
        }
        return null;
    }

    @Override
    public Link.Builder createLinkBuilder() {
        return null;
    }
}
