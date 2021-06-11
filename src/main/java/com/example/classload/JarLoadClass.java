package com.example.classload;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RestController
public class JarLoadClass implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    private static ApplicationContext applicationContext;
    private static BeanDefinitionRegistry beanDefinitionRegistry;
    private static ConfigurableListableBeanFactory configurableListableBeanFactory;
    private static Set<String> alreadyLoadClass = new HashSet<>();
    public static URLClassLoader urlClassLoader;

    @PostMapping({"/jarLoad"})
    public String jarLoad(@RequestBody String libDir) {
        try {
            File libPath = new File(libDir);
            // 获取所有的.jar和.zip文件
            File[] jarFiles = libPath.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            List<URL> urls = new ArrayList<>();
            for (File file : jarFiles) {
                URL url = file.toURI().toURL();
                urls.add(url);
            }
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), classLoader);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            //遍历每一个jar
            int count = handJarFiles(jarFiles);
            return "加载外部jar包的实例个数:" + count;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int handJarFiles(File[] jarFiles) throws Exception {
        HashSet<Class> needLoadClass = new HashSet<>();
        for (File file : jarFiles) {
            JarFile jarFile = new JarFile(file.getAbsoluteFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                // 如果是以/开头的
                if (name.charAt(0) == '/') {
                    // 获取后面的字符串
                    name = name.substring(1);
                }
                if (jarEntry.isDirectory() || !name.endsWith(".class")) {
                    continue;
                }
                String className = name.substring(0, name.length() - 6).replace("/", ".");
                //重复.class文件不加载
                if (!alreadyLoadClass.contains(className)) {
                    Class<?> beanClazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    alreadyLoadClass.add(beanClazz.getName());
                    needLoadClass.add(beanClazz);
                }
            }
        }
        //实例化和属性注入
        iocAndDI(needLoadClass);
        //使实例中的@Controller和@RequestMapping等注解生效
        processCandidateBean(needLoadClass);
        return needLoadClass.size();
    }

    private void processCandidateBean(Set<Class> extClass) throws Exception {
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Method processCandidateBean = AbstractHandlerMethodMapping.class.getDeclaredMethod("processCandidateBean", String.class);
        processCandidateBean.setAccessible(true);
        for (Class beanClazz : extClass) {
            String simpleName = beanClazz.getSimpleName();
            simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
            processCandidateBean.invoke(requestMappingHandlerMapping, simpleName);
        }
    }

    private static void iocAndDI(Set<Class> extClass) {
        for (Class beanClazz : extClass) {
            Component annotation = AnnotatedElementUtils.findMergedAnnotation(beanClazz, Component.class);
            if (annotation != null) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                beanDefinition.setBeanClass(beanClazz);
                beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                String simpleName = beanClazz.getSimpleName();
                simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                beanDefinitionRegistry.registerBeanDefinition(simpleName, beanDefinition);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws
            BeansException {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws
            BeansException {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

}
