package indi.kurok1.configuration.converter;

import org.eclipse.microprofile.config.spi.Converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * @author <a href="mailto:chan@ittx.com.cn">韩超</a>
 * @version 2021.07.14
 */
public class Converters {

    private final Map<Class, List<Converter>> cachedTypeConvertsMap = new ConcurrentHashMap<>();

    public void register(Class<?> clazz, Converter<?> converter, int order) {
        if (cachedTypeConvertsMap.containsKey(clazz)) {
            List<Converter> converters = cachedTypeConvertsMap.get(clazz);
            ListIterator<Converter> iterator = converters.listIterator();
            boolean added = false;
            while (iterator.hasNext()) {
                Converter current = iterator.next();
                if (current instanceof OrderedConverter) {
                    int currentOrder = ((OrderedConverter) current).getOrder();
                    if (currentOrder >= order) {
                        iterator.add(converter);
                        added = true;
                        break;
                    }
                } else {
                    //insert
                    iterator.add(converter);
                    added = true;
                    break;
                }
            }

            if (!added)
                converters.add(converter);

            cachedTypeConvertsMap.put(clazz, converters);
        } else {
            cachedTypeConvertsMap.put(clazz, new LinkedList<Converter>(Arrays.asList(converter)));
        }
    }

    public void register(Converter<?> converter) {
        Class<?> convertedType = resolveConvertedType(converter);
        int order = OrderedConverter.DEFAULT_ORDERED;
        if (converter instanceof OrderedConverter)
            order = ((OrderedConverter) converter).getOrder();
        register(convertedType, converter, order);
    }

    public void register(Converter converter, int order) {
        Class<?> convertedType = resolveConvertedType(converter);
        register(convertedType, converter, order);
    }

    protected Class<?> resolveConvertedType(Converter<?> converter) {
        if (converter == null)
            throw new NullPointerException();
        Class<?> convertedType = null;
        Class<?> converterClass = converter.getClass();
        while (converterClass != null) {
            convertedType = resolveConvertedType(converterClass);
            if (convertedType != null) {
                break;
            }

            Type superType = converterClass.getGenericSuperclass();
            if (superType instanceof ParameterizedType) {
                convertedType = resolveConvertedType(superType);
            }

            if (convertedType != null) {
                break;
            }
            // recursively
            converterClass = converterClass.getSuperclass();

        }

        return convertedType;
    }

    private Class<?> resolveConvertedType(Class<?> converterClass) {
        Class<?> convertedType = null;

        for (Type superInterface : converterClass.getGenericInterfaces()) {
            convertedType = resolveConvertedType(superInterface);
            if (convertedType != null) {
                break;
            }
        }

        return convertedType;
    }

    private Class<?> resolveConvertedType(Type type) {
        Class<?> convertedType = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            if (pType.getRawType() instanceof Class) {
                Class<?> rawType = (Class) pType.getRawType();
                if (Converter.class.isAssignableFrom(rawType)) {
                    Type[] arguments = pType.getActualTypeArguments();
                    if (arguments.length == 1 && arguments[0] instanceof Class) {
                        convertedType = (Class) arguments[0];
                    }
                }
            }
        }
        return convertedType;
    }

    public List<Converter> getConverters(Class<?> convertedType) {
        List<Converter> converters = cachedTypeConvertsMap.get(convertedType);
        return Collections.unmodifiableList(converters);
    }


}
