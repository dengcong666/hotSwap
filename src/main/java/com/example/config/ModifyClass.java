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

    private HotSwapper hs;

    @RequestMapping({"/changeMethod"})
    public String changeMethod(String className, String methodName, String logic) throws Exception {
        //需要引入 tools.jar
        //VM options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=40000  配置JRE
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(JarLoadClass.moduleClassLoader));
        CtClass ctClass = classPool.get(className);
        //解冻可以多次修改
        ctClass.defrost();
        CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);
        //{ return String.valueOf(666);}
        ctMethod.setBody(logic);
        if (hs == null) {
            hs = new HotSwapper(40000);
        }
        hs.reload(className, ctClass.toBytecode());
        return "改变了" + className + "." + methodName + "的执行逻辑: " + logic;
    }
}
