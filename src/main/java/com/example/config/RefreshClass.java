package com.example.config;

import com.example.classload.ModuleClassLoader;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
            //遍历每一个jar
            handJarFiles(jarFiles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handJarFiles(File[] jarFiles) throws Exception {
        System.out.println("加载外部jar包前实例个数:" + applicationContext.getBeanDefinitionCount());
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
                //实例化和属性注入
                iocAndDI(beanClazz);
            }
        }
        System.out.println("加载外部jar包之后实例个数:" + applicationContext.getBeanDefinitionCount());
        //使实例中的@Controller和@RequestMapping等注解生效
        DispatcherServlet dispatcherServlet = applicationContext.getBean(DispatcherServlet.class);
        refresh(dispatcherServlet);
    }

    private static void iocAndDI(Class<?> beanClazz) {
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

    private static void refresh(DispatcherServlet dispatcherServlet) {
        try {
            List<HandlerMapping> handlerMappings = dispatcherServlet.getHandlerMappings();
            for (HandlerMapping o : handlerMappings) {
                if (o instanceof RequestMappingHandlerMapping) {
                    ((RequestMappingHandlerMapping) o).afterPropertiesSet();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
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
