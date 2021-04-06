package com.example.config;

import com.example.classload.ModuleClassLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RestController
public class RefreshClass implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    public static ApplicationContext applicationContext;
    public static BeanDefinitionRegistry beanDefinitionRegistry;
    public static ConfigurableListableBeanFactory configurableListableBeanFactory;

    @RequestMapping({"/refresh"})
    public void refresh(@RequestBody String libDir) {
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
            ModuleClassLoader moduleClassLoader = new ModuleClassLoader(urls.toArray(new URL[urls.size()]), classLoader);
            Thread.currentThread().setContextClassLoader(moduleClassLoader);
            //先控制反转再依赖注入
            handJarFiles(jarFiles, "IOC");
            handJarFiles(jarFiles, "DI");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handJarFiles(File[] jarFiles, String iocOrDI) throws Exception {
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
                //加载.class文件 ,完成运行时的控制反转和依赖注入
                String className = name.substring(0, name.length() - 6).replace("/", ".");
                Class<?> beanClazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                if ("IOC".equals(iocOrDI)) {
                    IOC(beanClazz);
                }
                if ("DI".equals(iocOrDI)) {
                    DI(beanClazz);
                }
            }
        }
    }

    private void IOC(Class<?> beanClazz) {
        Component annotation = AnnotatedElementUtils.findMergedAnnotation(beanClazz, Component.class);
        if (annotation != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
            String simpleName = beanClazz.getSimpleName();
            simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());
            beanDefinitionRegistry.registerBeanDefinition(simpleName, builder.getBeanDefinition());
        }
    }

    private void DI(Class<?> beanClazz) {
        //注入容器对象
        String simpleName = beanClazz.getSimpleName();
        simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());
        Object bean = configurableListableBeanFactory.getBean(simpleName, beanClazz);
        Field[] declaredFields = beanClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(field, Autowired.class);
            Qualifier qualifier = AnnotatedElementUtils.findMergedAnnotation(field, Qualifier.class);
            Resource resource = AnnotatedElementUtils.findMergedAnnotation(field, Resource.class);
            if (autowired != null || qualifier != null || resource != null) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                String name = field.getName();
                try {
                    Map<String, ?> beansOfType = applicationContext.getBeansOfType(type);
                    if (beansOfType.size() == 1) {
                        field.set(bean, beansOfType.values().iterator().next());
                        System.out.println(beanClazz.getName() + "注入属性" + name);
                    } else if (beansOfType.size() > 1) {
                        field.set(bean, beansOfType.get(name));
                        System.out.println(beanClazz.getName() + "注入属性" + name);
                    }
                } catch (Exception e) {
                }
            }
            Value value = AnnotatedElementUtils.findMergedAnnotation(beanClazz, Value.class);
            if (value != null) {
                //TODO 注入Environment信息
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

}
