package com.infamous.performance.util;

/**
 * Created by h0rn3t on 22.09.2013.
 */
public class Prop implements Comparable<Prop> {

    private String name;
    private String data;

    public Prop(String n,String d){
        name = n;
        data = d;
    }
    public String getName(){
        return name;
    }
    public void setName(String d){
        this.name=d;

    }
    public String getVal(){
        return data;
    }
    public void setVal(String d){
        this.data=d;
    }
    public int compareTo(Prop o) {
        if(this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
