package com.shijia.github.springmvc.annotations;

import java.lang.annotation.*;

/**
 * controller注解
 * @Author jiahao
 * @Date 2020/9/19 19:08
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerBySelf {
    String value() default "";
}
