//package com.example.test;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class TestController {
//
//    //对@Value注解的测试
//    @Value(value = "${person.name}")
//    private String name;
//
//    //对@Autowired注解的测试
//    @Autowired
//    private Person person;
//
//    @RequestMapping({"/xxx"})
//    public String xxx() {
//        return name + person.getAge();
//    }
//}
