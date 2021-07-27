/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package indi.kurok1.interceptor;

import indi.kurok1.interceptor.microprofile.faulttolerance.BulkheadInterceptor;
import indi.kurok1.interceptor.microprofile.faulttolerance.EchoService;
import indi.kurok1.interceptor.microprofile.faulttolerance.TimeoutInterceptor;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * {@link ChainableInvocationContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ChainableInvocationContextTest {

    @Test
    public void test() throws Exception {
        EchoService echoService = new EchoService();
        Method method = EchoService.class.getMethod("echo", String.class);
        ReflectiveMethodInvocationContext delegateContext = new ReflectiveMethodInvocationContext
                (echoService, method, "Hello,World");

        Object[] interceptors = new Object[]{
                new TimeoutInterceptor(),
                new BulkheadInterceptor()
        };
        ChainableInvocationContext context = new ChainableInvocationContext(delegateContext, interceptors);

        context.proceed();

    }

    @Test
    public void test02() {
        Class<AnnotatedInterceptorRegistry> clazz = AnnotatedInterceptorRegistry.class;
        System.out.println(AbstractInterceptorRegistry.class.isAssignableFrom(clazz));
    }
}
