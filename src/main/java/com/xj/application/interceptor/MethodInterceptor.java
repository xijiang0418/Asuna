package com.xj.application.interceptor;

import com.xj.application.proxy.MethodInvoker;

public interface MethodInterceptor {

    public Object invoke(MethodInvoker methodInvoker) throws Throwable;

}
