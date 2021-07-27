package indi.kurok1.interceptor.jdkProxy;

import indi.kurok1.interceptor.impl.Notify;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 */
public interface TestService {

    @Retry(maxRetries = 3,
            delay = 0, maxDuration = 0, jitter = 0,
            retryOn = UnsupportedOperationException.class)
    @Notify(value = "Hello")
    void doSomething();

}
