package indi.kurok1.rest.converter;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * HTTP BODY 序列化和反序列化
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.20
 * @param <E> 请求实体类型
 * @param <R> 响应实体类型
 */
public interface HttpBodyConverter<E, R> extends MessageBodyReader<E>, MessageBodyWriter<R> {


}
