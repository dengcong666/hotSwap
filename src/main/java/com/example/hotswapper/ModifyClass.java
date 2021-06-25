package com.example.hotswapper;


import com.example.classload.JarLoadClass;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.spi.Connection;
import com.sun.tools.jdi.TargetVM;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.javassist.LoaderClassPath;
import org.apache.ibatis.javassist.util.HotSwapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;

//需要引入 tools.jar
//VM options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  配置JRE
@RestController
public class ModifyClass {

    @RequestMapping({"/changeMethod"})
    public String changeMethod(String className, String methodName, String logic) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(JarLoadClass.classLoader));
        CtClass ctClass = classPool.get(className);
        //解冻可以多次修改
        ctClass.defrost();
        CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);
        //{ return String.valueOf(666);}
        ctMethod.setBody(logic);
        HotSwapper hs = new HotSwapper(5005);
        hs.reload(className, ctClass.toBytecode());
        //Attach完成相应功能后，关闭Attaching连接
        closeAttachingConnection();
        return "改变了" + className + "." + methodName + "的执行逻辑: " + logic;
    }

    private void closeAttachingConnection() throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<VirtualMachine> virtualMachines = vmm.connectedVirtualMachines();
        for (VirtualMachine vm : virtualMachines) {
            Field targetField = vm.getClass().getDeclaredField("target");
            targetField.setAccessible(true);
            TargetVM target = (TargetVM) targetField.get(vm);
            Field connectionField = target.getClass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            Connection connection = (Connection) connectionField.get(target);
            connection.close();
        }
    }

}
