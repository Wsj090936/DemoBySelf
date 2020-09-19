package com.shijia.github.springmvc.annotations;

import java.lang.annotation.*;

/**
 *
 *
 * @Author jiahao
 * @Date 2020/9/19 19:14
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParamBySelf {
    String value();
}
