package indi.kurok1.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;

/**
 * echo "hello world" when {@link WebApplicationType} is {@link org.springframework.boot.WebApplicationType#NONE}
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.09.12
 */
public class HelloApplicationRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(HelloApplicationRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("detect current environment not in web application, echo : Hello World");
    }
}
