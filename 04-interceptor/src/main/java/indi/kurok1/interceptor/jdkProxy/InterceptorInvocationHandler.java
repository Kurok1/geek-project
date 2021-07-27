package indi.kurok1.interceptor.jdkProxy;

import indi.kurok1.interceptor.AnnotatedInterceptorRegistry;
import indi.kurok1.interceptor.ChainableInvocationContext;
import org.geektimes.commons.lang.Prioritized;

import javax.interceptor.Interceptors;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * JDK动态代理实现 {@link }
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 */
public class InterceptorInvocationHandler implements InvocationHandler {

    private final AnnotatedInterceptorRegistry registry;

    private final Object target;

    public InterceptorInvocationHandler(AnnotatedInterceptorRegistry registry, Object target) {
        this.registry = registry;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> resolvedInterceptors = new LinkedList<>();
        Annotation[] annotations = method.getDeclaredAnnotations();
        JdkProxyInvocationContext invocationContext = new JdkProxyInvocationContext(this.target, method, Collections.emptyMap());
        invocationContext.setParameters(args);
        //查找是否指定了拦截器
        for (Annotation annotation : annotations) {
            if (isInterceptors(annotation.getClass())) {
                Interceptors interceptors = (Interceptors) annotation;
                if (interceptors.value().length > 0) {
                    for (Class clazz : interceptors.value()) {
                        Optional<Object> interceptor = this.registry.getInterceptor(clazz);
                        interceptor.ifPresent((value) ->{
                            if (!resolvedInterceptors.contains(value))
                                resolvedInterceptors.add(value);
                        });
                    }
                }
                continue;
            }

            Object interceptor = this.registry.get(annotation.annotationType().getName());
            if (interceptor != null && !resolvedInterceptors.contains(interceptor))
                resolvedInterceptors.add(interceptor);
        }
        //没有指定拦截器，直接执行
        if (resolvedInterceptors.isEmpty()) {
            return invocationContext.proceed();
        }

        //制定了拦截器，链式调用
        Object[] array = resolvedInterceptors.toArray();
        //排序
        Arrays.sort(array, Prioritized.COMPARATOR);
        ChainableInvocationContext context = new ChainableInvocationContext(invocationContext, array);
        return context.proceed();
    }

    private boolean isInterceptors(Class<? extends Annotation> annotationClass) {
        return annotationClass == Interceptors.class;
    }

}
