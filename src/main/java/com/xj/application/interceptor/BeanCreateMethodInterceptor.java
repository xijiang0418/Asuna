package com.xj.application.interceptor;

import com.xj.application.annotation.Bean;
import com.xj.application.annotation.Component;
import com.xj.application.annotation.Configuration;
import com.xj.application.core.ApplicationContext;
import com.xj.application.proxy.MethodInvoker;

import java.lang.reflect.Method;

@Component
public class BeanCreateMethodInterceptor implements MethodInterceptor, Mark {


    @Override
    public Object invoke(MethodInvoker methodInvoker) throws Throwable {
        Method method = methodInvoker.getMethod();
        Bean bean = method.getAnnotation(Bean.class);
        if (bean != null) {
            if (!"".equals(bean.name())){
                return findOrCreateInstance(methodInvoker,bean.name());
            } else {
                return findOrCreateInstance(methodInvoker,method.getName());
            }
        }
        return methodInvoker.process();
    }

    private Object findOrCreateInstance(MethodInvoker methodInvoker, String name) throws Throwable {
        Object instance = ApplicationContext.getApplicationContext().getBean(name);
        if (instance == null) {
            Object process = methodInvoker.process();
            ApplicationContext.getApplicationContext().saveBean(name,process);
            return  process;
        } else {
            return instance;
        }

    }

    @Override
    public  Class getMark() {
        return Configuration.class;
    }
}
