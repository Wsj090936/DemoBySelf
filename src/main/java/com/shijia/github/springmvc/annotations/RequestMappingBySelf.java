package com.shijia.github.springmvc.annotations;

import java.lang.annotation.*;

/**
 *
 * @Author jiahao
 * @Date 2020/9/19 19:12
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMappingBySelf {
    String value() default "";
}
