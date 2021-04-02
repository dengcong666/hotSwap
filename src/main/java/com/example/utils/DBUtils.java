//package com.example.utils;
//
//import com.example.controller.CommonController;
//import org.apache.commons.dbutils.QueryRunner;
//import org.apache.commons.dbutils.handlers.MapListHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.AnnotatedElementUtils;
//import org.springframework.core.annotation.AnnotationUtils;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//import java.lang.reflect.Field;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class DBUtils {
//    @Autowired
//    private DataSource dataSource;
//
//    public List<Map<String, Object>> queryList(String sql,Object... params) {
//        try {
//            QueryRunner qr = new QueryRunner(dataSource);
//            List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler(), params);
//            return mapList;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
