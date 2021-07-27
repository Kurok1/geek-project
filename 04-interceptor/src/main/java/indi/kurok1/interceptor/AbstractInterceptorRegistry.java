package indi.kurok1.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 拦截器注册中心
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 * @param <K> 用于标识拦截器的泛型
 */
public abstract class AbstractInterceptorRegistry<K> extends ConcurrentHashMap<K, Object> {

    /**
     * 保留方法,加载拦截器
     */
    protected abstract void load();


    /**
     * 要求一个拦截器必须被 {@link javax.interceptor.InterceptorBinding}
     * 同时必须存在一个方法满足 {@link AbstractInterceptorRegistry#isInterceptMethod(java.lang.reflect.Method)}
     * @param interceptor 拦截器实例
     * @return 是否满足条件
     */
    protected boolean isValidateInterceptor(Object interceptor) {
        Class<?> clazz = interceptor.getClass();

        if (!clazz.isAnnotationPresent(Interceptor.class))
            return false;

        //public 方法才有意义
        Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0)
            return false;
        boolean flag = false;
        for (Method method : methods) {
            if (isInterceptMethod(method)) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    /**
     * 要求该方法被 {@link javax.interceptor.AroundInvoke} 标记，并且有且只有一个参数，其参数类型为 {@link javax.interceptor.InvocationContext}
     * @param method 目标方法
     * @return 是否满足条件
     */
    protected boolean isInterceptMethod(Method method) {
        if (method.isAnnotationPresent(AroundInvoke.class) && method.getParameterCount() == 1) {
            Parameter parameter = method.getParameters()[0];
            return InvocationContext.class.isAssignableFrom(parameter.getType());
        }

        return false;
    }


    public Optional<Object> getInterceptor(Class clazz) {
        if (isEmpty())
            return Optional.empty();

        return Stream.of(values().toArray())
                .filter(clazz::equals)
                .findFirst();

    }

}
