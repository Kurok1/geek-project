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
package indi.kurok1.cache.annotation.interceptor;

import indi.kurok1.cache.DataRepository;
import indi.kurok1.cache.InMemoryDataRepository;
import org.geektimes.interceptor.DefaultInterceptorEnhancer;
import org.geektimes.interceptor.Interceptor;
import org.geektimes.interceptor.InterceptorEnhancer;
import org.junit.Test;

import static org.geektimes.commons.util.ServiceLoaders.loadAsArray;
import static org.junit.Assert.*;

/**
 * {@link CacheResultInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CacheResultInterceptorTest {

    private DataRepository dataRepository = new InMemoryDataRepository();

    private InterceptorEnhancer enhancer = new DefaultInterceptorEnhancer();

    @Test
    public void test() {
        DataRepository repository = enhancer.enhance(dataRepository, DataRepository.class, loadAsArray(Interceptor.class));
        assertTrue(repository.save("A", 1));
        assertEquals(Integer.valueOf(1), repository.get("A"));
        assertEquals(Integer.valueOf(1), repository.getWithoutCache("A"));
    }
}