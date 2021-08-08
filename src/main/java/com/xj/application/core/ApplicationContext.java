package com.xj.application.core;

import com.xj.application.annotation.Aop;
import com.xj.application.annotation.Configuration;
import com.xj.application.annotation.Inject;
import com.xj.application.entity.BeanDefine;
import com.xj.application.interceptor.Mark;
import com.xj.application.interceptor.MethodInterceptor;
import com.xj.application.proxy.CglibProxyCreator;
import com.xj.application.proxy.JDKProxyCreator;
import com.xj.application.proxy.ProxyCreator;
import com.xj.application.util.ClassInfoResearchUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ApplicationContext {

    private static volatile ApplicationContext applicationContext ;

    private static final Map<Object, Object> IOC = new ConcurrentHashMap<>();

    private static List<BeanDefine> beanDefines;

    private static final String DEFAULT_CLASSPATH = "com.xj";

    private ApplicationContext(){
    }

    public static ApplicationContext getApplicationContext(String packageName) {

        if (applicationContext == null){
            synchronized (ApplicationContext.class){
                if (applicationContext == null) {
                    applicationContext = new ApplicationContext();
                    applicationContext.scanPackage(packageName);
                }
            }
        }
        return applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public void saveBean(String name,Object instance){
        if (!IOC.containsKey(name)) {
            IOC.put(name,instance);
        }
    }

    public  <T> T getBean(Class<T> type){
        Object obj = IOC.get(type);
        if (obj == null) {
            obj = createBean(type);
        }
        return (T) obj;
    }

    public Object getBean(String name){
        return IOC.get(name);
    }

    private void createBean(List<BeanDefine> beanDefines) {
        for (BeanDefine beanDefine : beanDefines) {
            if (IOC.containsKey(beanDefine.getType())){
                continue;
            }
            createBean(beanDefine.getType());
        }
    }

    private void initBean(Object instance,BeanDefine beanDefine) throws Exception {
        List<Method> methodInfo = beanDefine.getMethodInfo();
        for (Method method : methodInfo) {
            method.invoke(instance,method.getParameters());
        }
    }

    private  void populateBean(Object target) throws Exception {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            Inject inject = field.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
            }
            field.setAccessible(true);
            if (!inject.type().equals(Object.class)){
                Object bean = getBean(inject.type());
                if (bean.getClass().isAssignableFrom(inject.type())){
                    bean = getBean(inject.type());
                }
                field.set(target,bean);
            } else if (!"".equals(inject.name())) {
                Object bean = getBean(inject.name());
                field.set(target,bean);
            } else {
                int modifiers = field.getType().getModifiers();
                if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
                    Object instance = findInstance(field.getType());
                    field.set(target, instance);
                } else {
                    field.set(target, getBean(field.getType()));
                }
            }
        }
    }

    private Object findInstance(Class interfaceType){
        Set<Object> instanceTypeSet = IOC.keySet();
        Object instance = null;
        int count = 0;
        for (Object instanceType : instanceTypeSet) {
            if (instanceType instanceof Class) {
                if (interfaceType.isAssignableFrom((Class)instanceType)) {
                    count++;
                    instance = getBean((Class)instanceType);
                }
                if (count >= 2) {
                    throw new RuntimeException("有多个实现类，请指定");
                }
            }
        }
        if (count == 1){
            return instance;
        }
        return null;
    }

    private void scanPackage(String packageName){

        List<BeanDefine> defaultBeanDefines = ClassInfoResearchUtil.getClassInfoResearchUtil().ClassInfoScan(DEFAULT_CLASSPATH);
        List<BeanDefine> beanDefines = ClassInfoResearchUtil.getClassInfoResearchUtil().ClassInfoScan(packageName);
        beanDefines.addAll(defaultBeanDefines);
        this.beanDefines = beanDefines;


        for (BeanDefine beanDefine : beanDefines) {
            if (MethodInterceptor.class.isAssignableFrom(beanDefine.getType())) {
                createBean(beanDefine.getType());
            }
        }


        for (BeanDefine beanDefine : beanDefines) {
            if (beanDefine.getType().getAnnotation(Configuration.class)!=null) {
                createBean(beanDefine.getType());
            }
        }
        createBean(beanDefines);
    }

    private Object doCreatBean(Class type){
        Object instance = null;
        if (!IOC.containsKey(type)) {
            try {
                instance = type.getDeclaredConstructor().newInstance();
                IOC.put(instance.getClass(),instance);
                populateBean(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            instance = getBean(type);
        }
        return instance;
    }

    private void isNeedCreateProxy(Object instance){
        beanDefines.stream().filter(item -> item.getType().equals(instance.getClass())).forEach(item -> {
            ProxyCreator proxyCreator = null;
            for (Annotation annotation : item.getAnnotations()) {
                if (annotation instanceof Aop) {
                    proxyCreator = new JDKProxyCreator();
                } else if (annotation instanceof Configuration) {
                    proxyCreator = new CglibProxyCreator();
                }
                if (proxyCreator != null) {
                    Object proxyInstance = proxyCreator.createProxy(instance);
                    IOC.put(item.getType(),proxyInstance);
                    try {
                        initBean(proxyInstance,item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private Object createBean(Class type){
        Object bean = doCreatBean(type);
        isNeedCreateProxy(bean);
        return bean;
    }

    public static List<MethodInterceptor> getChain(Object instance){
        List<Object> methodInterceptorList = IOC.values().stream().filter(item -> MethodInterceptor.class.isAssignableFrom(item.getClass())).collect(Collectors.toList());
        List targetList = null;
        Annotation[] annotations = instance.getClass().getAnnotations();
        List<Class> annotationType = new ArrayList<>();
        for (Annotation annotation : annotations) {
            annotationType.add(annotation.annotationType());
        }
        if (annotationType.contains(Configuration.class)){
            targetList = methodInterceptorList.stream().filter(item -> Mark.class.isAssignableFrom(item.getClass()) && ((Mark)item).getMark().equals(Configuration.class)).collect(Collectors.toList());
        } else {
            targetList = methodInterceptorList.stream().filter(item -> !Mark.class.isAssignableFrom(item.getClass())).collect(Collectors.toList());
        }
        return targetList;
    }
}
