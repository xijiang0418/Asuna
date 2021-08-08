package com.xj.application.util;

import com.xj.application.annotation.Bean;
import com.xj.application.annotation.Configuration;
import com.xj.application.constant.InstanceType;
import com.xj.application.entity.BeanDefine;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassInfoResearchUtil {


    private volatile static ClassInfoResearchUtil classInfoResearchUtil;

    private static final String JAR_TAG = "jar";

    private static final String FILE_TAG = "file";

    private static final String FILE_END = ".class";


    private ClassInfoResearchUtil(){}

    public static ClassInfoResearchUtil getClassInfoResearchUtil(){

        if (classInfoResearchUtil == null) {
            synchronized (ClassInfoResearchUtil.class) {
                if (classInfoResearchUtil == null) {
                    return new ClassInfoResearchUtil();
                }
            }
        }
        return classInfoResearchUtil;
    }


    public List<BeanDefine> ClassInfoScan(String packageName){
        List<BeanDefine> list = new ArrayList<>();
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
            while (resources.hasMoreElements()){
                URL url = resources.nextElement();
                String protocol = url.getProtocol();
                if (protocol.equals(JAR_TAG)){
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = jarURLConnection.getJarFile();
                    handleJar(jarFile,list);
                } else if (protocol.equals(FILE_TAG)){
                    File file = new File(url.toURI());
                    handleFile(file,list);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void handleJar(JarFile jarFile,List<BeanDefine> list) throws Exception {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()){
            JarEntry jarEntry = entries.nextElement();
            String jarEntryName = jarEntry.getName();
            if (jarEntryName.endsWith(FILE_END)){
                String packageName = jarEntryName.substring(0, jarEntryName.length() - FILE_END.length()).replace("/", ".");
                doClassInfoScan(packageName,list);
            }
        }
    }

    private void handleFile(File file,List<BeanDefine> list) throws Exception {
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.isDirectory()) {
                handleFile(file1,list);
            } else if (file1.isFile() && file1.getName().endsWith(FILE_END)) {
                String path = ClassInfoResearchUtil.class.getClassLoader().getResource("").getPath();
                String packageName = file1.getPath().substring(path.length() -1, file1.getPath().length() - FILE_END.length()).replace(File.separator, ".");
                doClassInfoScan(packageName,list);
            }
        }
    }

    private void doClassInfoScan(String classpath,List<BeanDefine> list) throws Exception {
        BeanDefine beanDefine = new BeanDefine();
        Class<?> classType = Class.forName(classpath);
        beanDefine.setType(classType);
        boolean targetFlag = false;
        for (InstanceType type : InstanceType.values()) {
            Annotation annotation = classType.getAnnotation(type.getAnnotationType());
            if (annotation != null) {
                targetFlag = true;
                beanDefine.getAnnotations().add(annotation);
                if (annotation instanceof Configuration){
                    Method[] methods = classType.getDeclaredMethods();
                    for (Method method : methods) {
                        Bean bean = method.getAnnotation(Bean.class);
                        if (bean != null) {
                            beanDefine.getMethodInfo().add(method);
                        }
                    }
                }

            }
        }
        if (targetFlag) {
            list.add(beanDefine);
        }
    }

}
