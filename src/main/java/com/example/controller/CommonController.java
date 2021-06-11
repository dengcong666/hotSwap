//package com.example.controller;
//
//
//import com.google.gson.Gson;
//import org.apache.catalina.core.StandardContext;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//import java.util.*;
//
///**
// * <p>
// * 前端控制器
// * </p>
// *
// * @author dengcong
// * @since 2021-01-20
// */
//@RestController
//public class CommonController implements ApplicationContextAware {
//
//    private ApplicationContext applicationContext;
//
//    @RequestMapping({"/**"})
//    public String handler(@RequestBody String req, HttpServletRequest request) throws Exception {
//        String requestURI = request.getRequestURI();
//        String[] split = requestURI.split("/");
//        Object serviceBean = applicationContext.getBean(split[split.length - 2]);
//        Method[] methods = serviceBean.getClass().getMethods();
//        for (Method method : methods) {
//            //方法名相同，且方法实参是形参类型或其子类型时则调用。
//            if (method.getName().equals(split[split.length - 1])) {
//                Class<?>[] methodParameterTypes = method.getParameterTypes();
//                Class[] reqParameterTypes = getParameterTypes(req);
//                if (reqParameterTypes.length == methodParameterTypes.length) {
//                    boolean enable = true;
//                    for (int i = 0; i < reqParameterTypes.length; i++) {
//                        enable = enable && methodParameterTypes[i].isAssignableFrom(reqParameterTypes[i]);
//                    }
//                    if (enable) {
//                        Object invoke = method.invoke(serviceBean, linkedMapStringToArgs(req));
//                        return GSON.toJson(invoke);
//                    }
//                }
//            }
//        }
//        throw new RuntimeException("找不到执行方法" + split[split.length - 1]);
//    }
//
//    private static Class[] getParameterTypes(String req) throws Exception {
//        LinkedHashMap<String, Object> linkedHashMap;
//        if (StringUtils.isBlank(req)) {
//            linkedHashMap = new LinkedHashMap();
//        } else {
//            linkedHashMap = GSON.fromJson(req, LinkedHashMap.class);
//        }
//        Class[] classes = new Class[linkedHashMap.keySet().size()];
//        int index = 0;
//        for (String key : linkedHashMap.keySet()) {
//            Class<?> type = Class.forName(key.split("_")[0]);
//            classes[index] = type;
//            index++;
//        }
//        return classes;
//    }
//
//    private static Object[] linkedMapStringToArgs(String req) throws Exception {
//        LinkedHashMap<String, Object> linkedHashMap = GSON.fromJson(req, LinkedHashMap.class);
//        List<Object> params = new ArrayList();
//        for (Map.Entry<String, Object> entry : linkedHashMap.entrySet()) {
//            //防止两个参数类型相同时key冲突问题，类型_xxx
//            Class<?> type = Class.forName(entry.getKey().split("_")[0]);
//            if (StringUtils.isBlank(String.valueOf(entry.getValue()))) {
//                params.add(null);
//            } else {
//                Object value = GSON.fromJson(String.valueOf(entry.getValue()), type);
//                params.add(value);
//            }
//        }
//        return params.toArray();
//    }
//
//    private static final Gson GSON = new Gson();
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//}
