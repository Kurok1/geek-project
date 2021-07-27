package indi.kurok1.interceptor.impl;

import indi.kurok1.interceptor.AnnotatedInterceptor;
import org.geektimes.commons.lang.Prioritized;

import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * {@link Notify}实现
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 * @see AnnotatedInterceptor
 * @see Notify
 */
@Notify
@Interceptor
public class NotifyInterceptor extends AnnotatedInterceptor<Notify> {

    public NotifyInterceptor() throws IllegalArgumentException {
        setPriority(Prioritized.MIN_PRIORITY);
    }

    @Override
    protected Object execute(InvocationContext context, Notify bindingAnnotation) throws Throwable {
        Object result = context.proceed();

        String value = bindingAnnotation.value();

        System.out.println(value);
        return result;
    }


}
