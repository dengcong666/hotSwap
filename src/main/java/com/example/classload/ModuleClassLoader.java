package com.example.classload;

import java.net.URL;
import java.net.URLClassLoader;

public class ModuleClassLoader extends URLClassLoader {



    //构造
    public ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }


    //重写loadClass方法
    //改写loadClass方式
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            return super.loadClass(name);
        } else {
            return loadedClass;
        }
    }



}

