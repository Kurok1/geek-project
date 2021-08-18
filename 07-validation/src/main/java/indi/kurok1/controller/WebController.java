package indi.kurok1.controller;

import indi.kurok1.domain.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.18
 */
@RestController
@Validated
public class WebController implements InitializingBean {


    @PostMapping(value = "/api/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    public String foo(@RequestBody @NotNull User user) {
        return user.getName();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(11);
    }
}