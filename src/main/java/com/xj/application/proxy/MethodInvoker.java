package com.xj.application.proxy;

import com.xj.application.interceptor.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

public class MethodInvoker {

    private List<MethodInterceptor> chain;

    private int initStart;

    private Object target;

    private Method method;

    private MethodProxy methodProxy;

    private Object[] args;

    protected MethodInvoker(){}

    public Object process() throws Throwable {
        if (initStart < chain.size()) {
            MethodInterceptor methodInterceptor = chain.get(initStart);
            initStart++;
            return  methodInterceptor.invoke(this);
        }
        initStart = 0;
        if (methodProxy != null) {
            return methodProxy.invokeSuper(target,args);
        }
        return method.invoke(target,args);
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    protected MethodInvoker setChain(List<MethodInterceptor> chain) {
        this.chain = chain;
        return this;
    }

    protected MethodInvoker setTarget(Object target) {
        this.target = target;
        return this;
    }

    protected MethodInvoker setMethod(Method method) {
        this.method = method;
        return this;
    }

    protected MethodInvoker setMethodProxy(MethodProxy methodProxy) {
        this.methodProxy = methodProxy;
        return this;
    }

    protected MethodInvoker setArgs(Object[] args) {
        this.args = args;
        return this;
    }
}
