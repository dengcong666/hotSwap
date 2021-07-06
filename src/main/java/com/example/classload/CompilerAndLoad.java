package com.example.classload;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 需要引入tools.jar
 */
@RestController
public class CompilerAndLoad {

    @RequestMapping({"/javaFileLoad"})
    public String javaFileLoad(String dir) throws Exception {
        String path = System.getProperty("java.class.path");
        unJar(path, path.replace(".jar", ""));
        //将tools.jar拷贝到jre运行环境

        Boolean compiler = compiler(dir, path.replace(".jar", "") + "/BOOT-INF/lib");
        if (compiler.booleanValue()) {
            HashMap<String, String> extClassMap = getExtClassMap(dir);
            JarLoadClass.classLoader = new InnerClassLoad(JarLoadClass.classLoader, extClassMap);
            Thread.currentThread().setContextClassLoader(JarLoadClass.classLoader);
            HashSet<Class> extClassSet = getExtClassSet(extClassMap.keySet());
            JarLoadClass.iocAndDI(extClassSet);
            JarLoadClass.processCandidateBean(extClassSet);
            return "编译并加载到jvm成功";
        } else {
            return "编译失败";
        }

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
            classes.add(Thread.currentThread().getContextClassLoader().loadClass(classFullName));
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


    public Boolean compiler(String dir, String libDir) {
        StringBuilder classpath = new StringBuilder();
        File[] libs = new File(libDir).listFiles((p1, name) -> name.endsWith(".jar"));
        for (File file : libs) {
            classpath.append(file.getPath());
            classpath.append(File.pathSeparator);
        }
        try {
            //复制tools.jar到jre的lib目录下提供编译支持
            String toolsJarPath = System.getProperty("java.home") + "/lib/tools.jar";
            FileCopyUtils.copy(new FileInputStream(libDir + "/tools.jar"), new FileOutputStream(toolsJarPath));
        } catch (Exception e) {
        }
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
        File[] sourceCodeFilePath = new File(dir).listFiles((p1, name) -> name.endsWith(".java"));
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(sourceCodeFilePath);
        // 指定编译的class文件的路径
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(classpath.toString());
        JavaCompiler.CompilationTask cTask = javaCompiler.getTask(null, fileManager, null, options, null, fileObjects);
        Boolean success = cTask.call();
        if (success) {
            System.out.println("编译成功");
        } else {
            System.out.println("编译失败");
        }
        return success;
    }

    public static void unJar(String jarFile, String destination) {
        File jar = new File(jarFile);
        File dir = new File(destination);
        unJar(jar, dir);
    }

    /**
     * 解压当前的jar文件，其目录下的/BOOT-INF/lib提供动态编译支持
     *
     * @param jarFile     要解压的jar文件路径
     * @param destination 解压到哪里
     */
    public static void unJar(File jarFile, File destination) {
        JarFile jar = null;
        try {
            if (destination.exists() == false) {
                destination.mkdirs();
            }
            jar = new JarFile(jarFile);
            Enumeration<JarEntry> en = jar.entries();
            JarEntry entry = null;
            InputStream input = null;
            BufferedOutputStream bos = null;
            File file = null;
            while (en.hasMoreElements()) {
                entry = en.nextElement();
                input = jar.getInputStream(entry);
                file = new File(destination, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                } else {
                    file.getParentFile().mkdirs();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buffer = new byte[8192];
                int length = -1;
                while (true) {
                    length = input.read(buffer);
                    if (length == -1)
                        break;
                    bos.write(buffer, 0, length);
                }
                bos.close();
                input.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
