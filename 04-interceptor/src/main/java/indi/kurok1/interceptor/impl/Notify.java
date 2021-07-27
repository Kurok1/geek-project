package indi.kurok1.interceptor.impl;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * 执行完成后，执行一个通知操作
 *
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @version 2021.07.27
 * @see InterceptorBinding
 * @see System
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@InterceptorBinding
@Inherited
public @interface Notify {

    String value() default "";

}
