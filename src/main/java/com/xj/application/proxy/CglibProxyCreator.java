package com.xj.application.proxy;

import com.xj.application.core.ApplicationContext;


import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.List;

public class CglibProxyCreator implements MethodInterceptor,ProxyCreator, CallbackFilter {

    private MethodInvoker methodInvoker = new MethodInvoker();

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Object process = methodInvoker.setTarget(obj).setArgs(args).setMethod(method).setMethodProxy(proxy).process();
        return process;
    }

    @Override
    public Object createProxy(Object instance) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(instance.getClass());
        enhancer.setCallbackFilter(this::accept);
        enhancer.setCallbacks(new Callback[]{this,NoOp.INSTANCE});
        List<com.xj.application.interceptor.MethodInterceptor> chain = ApplicationContext.getChain(instance);
        methodInvoker.setChain(chain);
        return enhancer.create();
    }

    @Override
    public int accept(Method method) {
        if (method.getDeclaringClass().equals(Object.class)){
            return 1;
        }
        return 0;
    }
}
