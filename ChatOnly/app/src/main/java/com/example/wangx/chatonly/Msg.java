package com.example.wangx.chatonly;

import org.litepal.crud.LitePalSupport;

/**
 * Created by wangx on 2018/8/4.
 */

public class Msg extends LitePalSupport {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    private String content;

    public String getUsername() {
        return username;
    }

    private String username;//一串特殊的字符串
    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    private  int type;
    private int id;

    public Msg(int id, String content, int type, String username) {
        this.content = content;
        this.type = type;
        this.id = id;
        this.username = username;//随机生成的字符串
    }



}
