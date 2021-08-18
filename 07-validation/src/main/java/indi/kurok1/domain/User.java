package indi.kurok1.domain;

import javax.validation.constraints.NotNull;

/**
 * TODO
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.08.18
 */
public class User {

    @NotNull(groups = A.class)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static interface A {

    }
}


