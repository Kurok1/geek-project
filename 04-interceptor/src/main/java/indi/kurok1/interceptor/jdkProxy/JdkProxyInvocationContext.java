package indi.kurok1.interceptor.jdkProxy;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * JDK代理实现 {@link}
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 */
public class JdkProxyInvocationContext implements InvocationContext {

    private final Object target;

    private final Method method;

    private Object[] parameters = new Object[0];

    private final Map<String, Object> contextData;

    public JdkProxyInvocationContext(Object target, Method method, Map<String, Object> contextData) {
        if (target == null || method == null)
            throw new NullPointerException();
        this.target = target;
        this.method = method;
        this.contextData = contextData;
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public void setParameters(Object[] params) {
        this.parameters = params;
    }

    @Override
    public Map<String, Object> getContextData() {
        return this.contextData;
    }

    @Override
    public Object proceed() throws Exception {
        return method.invoke(target, parameters);
    }
}
