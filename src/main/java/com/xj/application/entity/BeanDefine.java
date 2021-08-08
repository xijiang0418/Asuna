package com.xj.application.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanDefine {

    private Class type;

    private List<Annotation> annotations = new ArrayList<>();

    private List<Method> MethodInfo = new ArrayList<>();

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<Method> getMethodInfo() {
        return MethodInfo;
    }

    @Override
    public String toString() {
        return "BeanDefine{" +
                "type=" + type +
                ", annotations=" + annotations +
                ", MethodInfo=" + MethodInfo +
                '}';
    }
}
