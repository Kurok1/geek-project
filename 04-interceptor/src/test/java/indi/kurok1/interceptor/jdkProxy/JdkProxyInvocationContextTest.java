package indi.kurok1.interceptor.jdkProxy;

import indi.kurok1.interceptor.AnnotatedInterceptorRegistry;
import indi.kurok1.interceptor.impl.Notify;
import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 */
public class JdkProxyInvocationContextTest {

    @Test
    public void test() {
        AnnotatedInterceptorRegistry registry = new AnnotatedInterceptorRegistry();
        registry.addSupportAnnotationTypes(Notify.class);
        Class[] classes = new Class[]{TestService.class};
        TestService instance = (TestService) Proxy.newProxyInstance(getClass().getClassLoader(), classes, new InterceptorInvocationHandler(registry, new TestServiceImpl()));
        instance.doSomething();
    }

    static class TestServiceImpl implements TestService {
        @Override
        public void doSomething() {
            System.out.println("working!!!");
        }
    }

}
