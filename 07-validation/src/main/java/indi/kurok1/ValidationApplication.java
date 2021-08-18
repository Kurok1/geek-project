package indi.kurok1;

import indi.kurok1.controller.WebController;
import indi.kurok1.domain.User;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.18
 */
@SpringBootApplication
public class ValidationApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ValidationApplication.class, args);
        WebController webController = run.getBean(WebController.class);
        webController.foo(null);
    }

    @Bean
    public BeanValidationPostProcessor beanValidationPostProcessor () {
        return new BeanValidationPostProcessor();
    }

}
