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
package com.salesmanager.shop.spring.cloud.client.serviceregistry;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureBefore({AutoServiceRegistrationAutoConfiguration.class})
public class NoOpAutoServiceRegistrationAutoConfiguration {

    @Bean
    @Primary
    public NoOpServiceRegistry noOpServiceRegistry() {
        return new NoOpServiceRegistry();
    }

    @Bean
    @Primary
    public NoOpAutoServiceRegistration noOpAutoServiceRegistration() {
        return new NoOpAutoServiceRegistration();
    }

    @Bean
    @Primary
    public NoOpRegistration noOpRegistration(){
        return new NoOpRegistration();
    }
}
