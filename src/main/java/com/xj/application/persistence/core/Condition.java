package com.xj.application.persistence.core;

import java.util.ArrayList;
import java.util.List;

public class Condition {

    private Class target;

    private static StringBuilder sb = new StringBuilder(" where ");

    private List<Object> params = new ArrayList<>();

    public Condition(Class target){
        this.target = target;
    }

    private String getColumnName(String fieldName){
        try {
            return target.getDeclaredField(fieldName).getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public Condition equals(String fieldName,Object property){
        sb.append(getColumnName(fieldName)).append("= ? ");
        params.add(property);
        return this;
    }

    public Condition and(){
        sb.append("and ");
        return this;
    }

    public Condition or(){
        sb.append("or ");
        return this;
    }

    public Condition moreThan(String fieldName,Object property){
        sb.append(getColumnName(fieldName)).append("> ? ");
        params.add(property);
        return this;
    }

    public Condition lessThan(String fieldName,Object property){
        sb.append(getColumnName(fieldName)).append("< ? ");
        params.add(property);
        return this;
    }

    public Condition moreThanEquals(String fieldName,Object property){
        sb.append(getColumnName(fieldName)).append(">= ? ");
        params.add(property);
        return this;
    }

    public Condition lessThanEquals(String fieldName,Object property){
        sb.append(getColumnName(fieldName)).append(">= ? ");
        params.add(property);
        return this;
    }

}
