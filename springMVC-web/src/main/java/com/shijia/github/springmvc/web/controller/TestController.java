package com.shijia.github.springmvc.web.controller;

import com.shijia.github.springmvc.annotations.ControllerBySelf;
import com.shijia.github.springmvc.annotations.RequestMappingBySelf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @Author jiahao
 * @Date 2020/9/20 13:53
 */
@ControllerBySelf
@RequestMappingBySelf("/test")
public class TestController {
    @RequestMappingBySelf("/doTest")
    public void doTest(HttpServletRequest request, HttpServletResponse response){
        System.out.println("Jinlail");
    }
}
