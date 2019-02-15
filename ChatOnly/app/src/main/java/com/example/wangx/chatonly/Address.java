package com.example.wangx.chatonly;

import org.litepal.crud.LitePalSupport;

/**
 * Created by wangx on 2019/1/2.
 */

public class Address extends LitePalSupport{
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    private String address1;
    private String address2;
}
