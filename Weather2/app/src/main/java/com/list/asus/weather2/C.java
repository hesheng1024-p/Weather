package com.list.asus.weather2;

import java.util.ArrayList;


public class C {

    public static ArrayList<String> cityNameArry = new ArrayList<>();

    public static void add(ArrayList<String> arrayList, String string){
        int i=0;
        for (String a: arrayList){
            if (! a.equals(string)){
                i++;
            }
        }
        if (arrayList.size() == i || string != null  || arrayList.size() < 9){
            arrayList.add(string);
        }
    }
    public static boolean Judge(ArrayList<String> arrayList, String string) {
        int i=0;
        for (String a: arrayList){
            if (! a.equals(string)){
                i++;
            }
        }
        if (arrayList.size() == i){
            return true;
        } else {
            return false;
        }
    }
}
