//package com.example.service.impl;
//
//import com.example.model.auto.Bank;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * <p>
// * 服务实现类
// * </p>
// *
// * @author dengcong
// * @since 2021-01-20
// */
//@Service
//public class BankServiceImpl {//extends ServiceImpl<BankMapper, Bank> {
//
//    @Autowired
//    private com.example.utils.DBUtils dbUtils;
//
//    public List<Map<String, Object>> getBank(Integer id) {
//        List<Map<String, Object>> maps = dbUtils.queryList("select * from bank where id=" + id);
//        return maps;
//    }
//
//    public List<Map<String, Object>> getBank(Integer id, String name) {
//        List<Map<String, Object>> maps = dbUtils.queryList("select * from bank where id=? and name=?", id, name);
//        return maps;
//    }
//
//    public List<Map<String, Object>> getBank(Bank bank) {
//        List<Map<String, Object>> maps = dbUtils.queryList("select * from bank where id=" + bank.getId());
//        return maps;
//    }
//
//}
