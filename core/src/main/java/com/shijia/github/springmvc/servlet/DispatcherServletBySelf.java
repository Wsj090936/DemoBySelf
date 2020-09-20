package com.shijia.github.springmvc.servlet;

import com.shijia.github.springmvc.annotations.ControllerBySelf;
import com.shijia.github.springmvc.annotations.RequestMappingBySelf;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 *
 * @Author jiahao
 * @Date 2020/9/19 19:22
 */
public class DispatcherServletBySelf extends HttpServlet {
    Properties properties = new Properties();

    // 存放所有带类名
    List<String> classNames = new ArrayList<>();
    Map<String,Object> ioc = new HashMap<>();
    private Map<String, Method> handlerMapping = new  HashMap<>();
    private Map<String,Object> controllerMap = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req,resp);
    }

    // 最重要的功能，做请求转发
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1、获取URL
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();// 项目名
        requestURI = requestURI.replace(contextPath,"").replaceAll("/+","/");
        if(!handlerMapping.containsKey(requestURI)){
            resp.getWriter().write("404 NOT FUND");
            return;
        }
        Method method = this.handlerMapping.get(requestURI);
        // 2、获取请求参数
        // 获取所有方法的参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String, String[]> parameterMap = req.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];
        for(int i = 0; i < parameterTypes.length;i++){
            // 根据参数类型，解析
            String requestParam = parameterTypes[i].getSimpleName();
            if(requestParam.equals("HttpServletRequest")){
                paramValues[i] = req;
                continue;
            }
            if(requestParam.equals("HttpServletResponse")){
                paramValues[i] = resp;
                continue;
            }
            if(requestParam.equals("String")){
                for(Map.Entry<String,String[]> param : parameterMap.entrySet()){
                    String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }


        }
        // 3、利用反射执行方法
        try {
            method.invoke(controllerMap.get(requestURI),paramValues);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1、加载配置文件
            loadConfig(config.getInitParameter("contextConfigLocation"));
        // 2、扫描注解类
            scanPackage(properties.getProperty("scanPackage"));
        // 3、根据扫描到的类，初始化IOC容器
            doInstance();
        // 4、初始化handlerMapping 存储url - method的映射
            initHandlerMapping();


    }

    private void initHandlerMapping() {
        if(ioc.isEmpty()){
            return;
        }
        try {
            for(Map.Entry each : ioc.entrySet()){
                Class<?> clazz = each.getValue().getClass();

                if(!clazz.isAnnotationPresent(ControllerBySelf.class)){
                    continue;
                }
                // 存储url对应方法时，需要加上类外部的url
                String baseUrl = "";
                if(clazz.isAnnotationPresent(RequestMappingBySelf.class)){
                    RequestMappingBySelf annotation = clazz.getAnnotation(RequestMappingBySelf.class);
                    baseUrl = annotation.value().replaceAll("/+", "/");
                }
                // 继续拼装方法级别的url
                for (Method method : clazz.getMethods()) {
                    if(!method.isAnnotationPresent(RequestMappingBySelf.class)){
                        continue;
                    }
                    String methodUrl = "";
                    RequestMappingBySelf methodAnnotation = method.getAnnotation(RequestMappingBySelf.class);
                    methodUrl = baseUrl + methodAnnotation.value().replaceAll("/+", "/");
                    // 方法url -对应的方法
                    handlerMapping.put(methodUrl,method);
                    // 方法url-对应的controller类
                    controllerMap.put(methodUrl,clazz.newInstance());

                }

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }

    private void doInstance() {
        if(classNames.isEmpty()){
            return;
        }

        for(String className : classNames){
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(ControllerBySelf.class)){
                    // 遇到带有ControllerBySelf的注解，需要进行初始化
                    ioc.put(toLowerFirstWord(className),clazz.newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

    private void scanPackage(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for(File eachFile : dir.listFiles()){
            if(eachFile.isDirectory()){
                // 遇到文件夹，就递归
                scanPackage(packageName + "." + eachFile.getName());
            }else {
                // 遇到类，就将其加载
                String className = packageName + "." + eachFile.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件
     * @param location
     */
    private void loadConfig(String location) {
        //
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            // 将applicationContext.properties中的内容加载进流中
            properties.load(resourceAsStream);
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
