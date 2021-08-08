package com.xj.application.proxy;

import com.xj.application.core.ApplicationContext;
import com.xj.application.interceptor.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class JDKProxyCreator implements InvocationHandler,ProxyCreator{


    private MethodInvoker methodInvoker = new MethodInvoker();


    @Override
    public Object createProxy(Object instance) {
        List<MethodInterceptor> chain = ApplicationContext.getChain(instance);
        methodInvoker.setTarget(instance).setChain((chain));
        return Proxy.newProxyInstance(instance.getClass().getClassLoader(),instance.getClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(methodInvoker);
        Object invoke = methodInvoker.setMethod(method).setArgs(args).process();
        return invoke;
    }

}
