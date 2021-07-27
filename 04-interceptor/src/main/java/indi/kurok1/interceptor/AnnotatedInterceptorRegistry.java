package indi.kurok1.interceptor;

import org.eclipse.microprofile.faulttolerance.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于 {@link AnnotatedInterceptor}的注册中心
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 * @see AbstractInterceptorRegistry
 * @see AnnotatedInterceptor
 */
public final class AnnotatedInterceptorRegistry extends AbstractInterceptorRegistry<String> {

    private final CopyOnWriteArrayList<Class<? extends Annotation>> supportAnnotationTypes = new CopyOnWriteArrayList<>(Arrays.asList(
            Asynchronous.class, Bulkhead.class, CircuitBreaker.class, Fallback.class, Retry.class, Timeout.class
    ));

    public void addSupportAnnotationTypes(Class<? extends Annotation> newSupportAnnotation) {
        this.supportAnnotationTypes.addIfAbsent(newSupportAnnotation);
    }

    public boolean isSupport(Class<? extends Annotation> supportAnnotation) {
        return this.supportAnnotationTypes.contains(supportAnnotation);
    }

    public AnnotatedInterceptorRegistry() {
        load();
    }

    @Override
    protected void load() {
        //spi加载, 唯一标识符为注解的全限定名称
        ServiceLoader<AnnotatedInterceptor> serviceLoader = ServiceLoader.load(AnnotatedInterceptor.class);
        Iterator<AnnotatedInterceptor> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            //获取注解
            AnnotatedInterceptor interceptor = iterator.next();
            Class<? extends Annotation> annotationClass =  interceptor.getBindingAnnotationType();
            if (isValidateInterceptor(interceptor)) {
                putIfAbsent(annotationClass.getName(), interceptor);
            } else {
                System.out.printf("the interceptor [%s] is not validated. skip it! \n", interceptor.getClass().getName());
            }
        }
    }
}
