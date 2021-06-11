package com.example.config;


import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.javassist.LoaderClassPath;
import org.apache.ibatis.javassist.util.HotSwapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModifyClass {

    //不要使用断点模式启动应用
    private static HotSwapper hs;

    static {
        try {
            hs = new HotSwapper(40000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping({"/changeMethod"})
    public String changeMethod(String p1, String p2, String p3) throws Exception {
        //需要引入 tools.jar
        //VM options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8910  配置JRE
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(JarLoadClass.moduleClassLoader));
        CtClass ctClass = classPool.get(p1);
        //解冻可以多次修改
        ctClass.defrost();
        CtMethod ctMethod = ctClass.getDeclaredMethod(p2);
        //{ return String.valueOf(666);}
        ctMethod.setBody(p3);

        hs.reload(p1, ctClass.toBytecode());
        return "改变了" + p1 + "." + p2 + "的执行逻辑";
    }
}
