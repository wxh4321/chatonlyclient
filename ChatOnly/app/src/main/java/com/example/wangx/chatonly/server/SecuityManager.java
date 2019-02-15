package com.example.wangx.chatonly.server;

/**
 * Created by wangx on 2019/1/28.
 */

public class SecuityManager {
    public String Encryption(String code,String index)
    {
        String new_str = "";
        if(code == null||code ==""){
            return "";
        }
        //加密
        for(int i = 0;i<code.length();i++){
            char a = code.toCharArray()[i];
            char a1 = (char)((int)a+Integer.valueOf(index));
            new_str+=a1;
        }
        return new_str;
    }
    public String Deciphering(String code,String index){
        if(code == null||code ==""){
            return "";
        }
        //解密
        String a_back = "";
        for(int i = 0;i<code.length();i++){
            char a = code.toCharArray()[i];
            char a1 = (char)((int)a-Integer.valueOf(index));
            a_back+=a1;
        }
        return a_back;
    }
}
