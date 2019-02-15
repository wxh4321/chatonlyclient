package com.example.wangx.chatonly.server;

import com.example.wangx.chatonly.Address;
import com.example.wangx.chatonly.UserId;

import org.litepal.LitePal;

/**
 * Created by wangx on 2019/1/28.
 */

public class DataBaseManager {
    public String find_address()
    {
        try{
            Address ad = LitePal.find(Address.class, 1);
            return ad.getAddress1()+","+ad.getAddress2();
        }
        catch (Exception e){
            return "";
        }
    }
    public String findUserName()
    {
        try{
            UserId userid = LitePal.find(UserId.class, 1);
            return userid.getUsername();
        }
        catch (Exception e){
            return "";
        }
    }
}
