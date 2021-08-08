package com.xj.application.persistence.core;


import com.xj.application.annotation.Table;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class Executor {

    private DataSource dataSource;

    //保存基本数据类型
    private static final List<String> BASIC_TYPE = Arrays.asList("byte","short","int","long","float","double","char");


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void close(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            if(resultSet!=null) {
                resultSet.close();
            }
            if(statement!=null) {
                statement.close();
            }
            if(connection!=null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement getStatement(String sql, Connection connection, Object... args)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i+1, args[i]);
        }

        return statement;
    }

    public <T> List<T> query(String sql, Class<T> type,Object... args){
        List<T> list = doQuery(sql, type, args);
        return list;
    }

    private <T> List<T> doQuery(String sql, Class<T> type,Object... args){
        Map<Class,List<Object>> targetsConnection = new LinkedHashMap<>();
        List<Class> types = new ArrayList<>();
        List<T> list = new ArrayList<T>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = getStatement(sql, getConnection(), args);
            resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<T> BeanCache = new ArrayList<>();
            getTargetsType(type,targetsConnection,types);
            while (resultSet.next()) {
                Map<Class, Object> beanMap = setTargetValue(resultSet, metaData, types);
                checkRelation(list,type,beanMap,BeanCache,targetsConnection);
            }
            if (!BeanCache.isEmpty()) {
                list.addAll(BeanCache);
                BeanCache.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(resultSet,statement,null);
        }
        return list;
    }


    private <T> void doCheckRelation(Object currentInstance,Map<Class,List<Object>> targetsConnection,Map<Class, Object> beanMap,List<T> beanCache,List<T> list) throws Exception {
        List<Object> typeInfoList = targetsConnection.get(currentInstance.getClass());
        if (typeInfoList != null) {
            String collectionFieldName = (String) typeInfoList.get(0);
            com.xj.application.annotation.Collection collection = (com.xj.application.annotation.Collection) typeInfoList.get(1);

            Object next = null;
            Field collectionField = currentInstance.getClass().getDeclaredField(collectionFieldName);
            collectionField.setAccessible(true);
            Object collectionValue = collectionField.get(currentInstance);
            if (collectionValue instanceof List) {
                List listCollection =  (List)collectionValue;
                next = listCollection.get(listCollection.size() - 1 );
            } else if (collectionValue instanceof Set) {


            }

            doCheckRelation(next,targetsConnection,beanMap,beanCache,list);

            String foreignID = collection.foreignID();
            String primaryID = collection.primaryID();
            String nextBeanID = collection.nextBeanID();

            Field primaryIDField = currentInstance.getClass().getDeclaredField(primaryID);
            Object nextInstance = beanMap.get(collection.type());
            Field foreignIDField = nextInstance.getClass().getDeclaredField(foreignID);
            Field nextBeanIDField = next.getClass().getDeclaredField(nextBeanID);
            Field nextInstanceIDField = nextInstance.getClass().getDeclaredField(nextBeanID);

            primaryIDField.setAccessible(true);
            foreignIDField.setAccessible(true);
            nextBeanIDField.setAccessible(true);
            nextInstanceIDField.setAccessible(true);

            Object currentInstanceIDValue = primaryIDField.get(currentInstance);
            Object nextInstanceForeignIDValue = foreignIDField.get(nextInstance);
            Object nextBeanIDValue = nextBeanIDField.get(next);
            Object nextInstanceIDValue = nextInstanceIDField.get(nextInstance);

            if (currentInstanceIDValue.equals(nextInstanceForeignIDValue) && !nextBeanIDValue.equals(nextInstanceIDValue)) {
                ((Collection)(collectionValue)).add(nextInstance);
            } else if (!nextBeanIDValue.equals(nextInstanceIDValue)) {
                if (currentInstance.getClass().equals(beanCache.get(0).getClass())) {
                    list.addAll(beanCache);
                    Object o = beanMap.get(beanCache.get(0).getClass());
                    beanCache.clear();
                    beanCache.add((T) o);
                }
            }
        }
    }

    private <T> void checkRelation(List<T> list,Class<T> type,Map<Class, Object> beanMap,List<T> beanCache,Map<Class,List<Object>> targetsConnection) throws Exception {
        T instance = linkedRelation(type, beanMap);
        if (beanCache.isEmpty()){
            beanCache.add(instance);
        } else {
            doCheckRelation(beanCache.get(0),targetsConnection,beanMap,beanCache,list);
        }
    }

    private <T> T linkedRelation(Class<T> targetType, Map<Class, Object> beans) throws Exception {
        List<Class> beanPopulateCache = new ArrayList<>();
        Object currentBean = beans.get(targetType);
        beanPopulateCache.add(targetType);
        doSetTargetValue(beans,currentBean,beanPopulateCache);
        return (T) currentBean;
    }


    private <T> Map<Class,Object> setTargetValue (ResultSet resultSet,ResultSetMetaData metaData,List<Class> types) throws Exception {
        Map<String,List<Object>> row = new HashMap<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (row.containsKey(metaData.getTableName(i))){
                List<Object> valueList = row.get(metaData.getTableName(i));
                valueList.add(metaData.getColumnName(i));
                valueList.add(resultSet.getObject(i));
            } else {
                List<Object> valueList = new ArrayList<>();
                valueList.add(metaData.getColumnName(i));
                valueList.add(resultSet.getObject(i));
                row.put(metaData.getTableName(i),valueList);
            }
        }

        Map<Class,Object> beans = new HashMap<>();

        for (Class type : types) {
            Table table = (Table) type.getAnnotation(Table.class);
            if (table != null && row.containsKey(table.name())){
                List<Object> valueList = row.get(table.name());
                Object instance = reflectInstance(type);
                beans.put(instance.getClass(),instance);
                setFieldValue(instance,valueList);
            } else {
                throw new RuntimeException("类与表名不匹配 type" + type);
            }
        }
        return beans;
    }

    private void doSetTargetValue(Map<Class,Object> beans,Object currentBean,List<Class> cacheList) throws Exception {
        Field[] fields = currentBean.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            com.xj.application.annotation.Collection collection = field.getAnnotation(com.xj.application.annotation.Collection.class);
            Class<?> type = field.getType();
            if (collection != null) {
                type = collection.type();
            }
            if (beans.containsKey(type)){
                if (!cacheList.contains(type)) {
                    cacheList.add(type);
                    doSetTargetValue(beans, beans.get(type), cacheList);
                    Object properties = field.get(currentBean);
                    if (properties instanceof List) {
                        ((List) properties).add(beans.get(type));
                    } else if (properties instanceof Set) {
                        ((Set)properties).add(beans.get(type));
                    } else {
                        field.set(currentBean, beans.get(type));
                    }
                }
            }
        }
    }

    private <T> T reflectInstance(Class<T> type) throws Exception {
        return type.getDeclaredConstructor().newInstance();
    }

    private <T> void setFieldValue (T target,List<Object> valueList) throws Exception {
        for (int i = 0; i < valueList.size(); i++) {
            Field field = target.getClass().getDeclaredField(valueList.get(i).toString());
            field.setAccessible(true);
            field.set(target,valueList.get(++i));
        }
    }

    private <T> void getTargetsType(Class<T> target,Map<Class,List<Object>> targetsConnection,List<Class> types) throws Exception {
        types.add(target);
        Field[] fields = target.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            String fieldTypeName = field.getType().getName();
            if (Collection.class.isAssignableFrom(fieldType)) {
                com.xj.application.annotation.Collection collection = field.getAnnotation(com.xj.application.annotation.Collection.class);
                if (collection != null) {
                    List<Object> infoList = new ArrayList<>();
                    infoList.add(field.getName());
                    infoList.add(collection);
                    targetsConnection.put(target, infoList);
                    getTargetsType(collection.type(), targetsConnection,types);
                }
            } else if (!fieldTypeName.contains("java") && !BASIC_TYPE.contains(fieldTypeName)) {
                if (!targetsConnection.containsKey(field.getType())) {
                    targetsConnection.put(field.getType(), null);
                    getTargetsType(fieldType, targetsConnection,types);
                }
            }
        }
    }
}
