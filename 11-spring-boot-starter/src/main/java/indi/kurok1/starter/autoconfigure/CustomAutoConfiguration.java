package indi.kurok1.starter.autoconfigure;

import indi.kurok1.starter.HelloApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * condition on not web application,pay attention to the existence of spring boot web dependencies.
 *
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.09.12
 */
@ConditionalOnMissingClass(value = {"org.springframework.web.context.support.GenericWebApplicationContext", "org.springframework.web.reactive.HandlerResult"})
@ConditionalOnNotWebApplication
@Configuration
public class CustomAutoConfiguration {

    @Bean
    public HelloApplicationRunner runner() {
        return new HelloApplicationRunner();
    }

}
