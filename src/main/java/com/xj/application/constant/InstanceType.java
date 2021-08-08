package com.xj.application.constant;

import com.xj.application.annotation.Aop;
import com.xj.application.annotation.Component;
import com.xj.application.annotation.Configuration;
import com.xj.application.annotation.Service;

public enum InstanceType {

    COMPONENT(Component.class),
    SERVICE(Service.class),
    AOP(Aop.class),
    CONFIGURATION(Configuration.class);

    private Class annotation;

    InstanceType(Class annotation) {
        this.annotation = annotation;
    }

    public Class getAnnotationType(){
        return annotation;
    }


}
