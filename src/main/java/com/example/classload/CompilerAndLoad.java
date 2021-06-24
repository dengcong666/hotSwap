package com.example.classload;


import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.*;

/**
 * 需要引入tools.jar
 */
@RestController
public class CompilerAndLoad {

    @RequestMapping({"/javaFileLoad"})
    public String javaFileLoad(String dir) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File[] javaFiles = new File(dir).listFiles(pathname -> pathname.getName().endsWith(".java"));
        HashSet<String> javaFilePaths = new HashSet<>();
        for (File file : javaFiles) {
            javaFilePaths.add(file.getPath());
        }
        int run = compiler.run(null, null, null, javaFilePaths.toArray(new String[javaFilePaths.size()]));
        if (run != 0) {
            return "编译失败";
        }
        HashMap<String, String> extClassMap = getExtClassMap(dir);
        JarLoadClass.classLoader = new InnerClassLoad(JarLoadClass.classLoader, extClassMap);
        HashSet<Class> extClassSet = getExtClassSet(extClassMap.keySet());
        JarLoadClass.iocAndDI(extClassSet);
        JarLoadClass.processCandidateBean(extClassSet);
        return "编译并加载到jvm成功";
    }


    private static class InnerClassLoad extends ClassLoader {
        private HashMap<String, String> classPathMapping;

        public InnerClassLoad(ClassLoader parent, HashMap<String, String> classPathMapping) {
            super(parent);
            this.classPathMapping = classPathMapping;
        }

        @Override
        protected Class<?> findClass(final String name)
                throws ClassNotFoundException {
            try {
                String path = classPathMapping.get(name);
                InputStream in = new FileInputStream(new File(path));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int len;
                while (true) {
                    try {
                        if (((len = in.read(bytes)) == -1)) break;
                        baos.write(bytes, 0, len);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bytes = baos.toByteArray();
                Class clazz = defineClass(name, bytes, 0, bytes.length);
                return clazz;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 类加载器加载class
     */
    private static HashSet<Class> getExtClassSet(Set<String> classFullNames) throws Exception {
        HashSet<Class> classes = new HashSet<>();
        for (String classFullName : classFullNames) {
            classes.add(JarLoadClass.classLoader.loadClass(classFullName));
        }
        return classes;
    }

    /**
     * 定义classFullName与class文件所在路径的映射关系
     */
    private static HashMap<String, String> getExtClassMap(String dir) {
        try {
            HashMap<String, String> classFullNameMapPath = new HashMap<>();
            File[] javaFiles = new File(dir).listFiles(pathname -> pathname.getName().endsWith(".java"));
            for (File javaFile : javaFiles) {
                FileReader fileReader = new FileReader(javaFile);
                String string = FileCopyUtils.copyToString(fileReader);
                String[] split = string.split("\n");
                for (String line : split) {
                    if (line.contains("package")) {
                        line = line.split(";")[0];
                        String aPackage = line.replace("package", "").trim();
                        String fullClassName = aPackage + "." + javaFile.getName().replace(".java", "");
                        classFullNameMapPath.put(fullClassName, javaFile.getPath().replace(".java", ".class"));
                    }
                }
            }
            return classFullNameMapPath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
