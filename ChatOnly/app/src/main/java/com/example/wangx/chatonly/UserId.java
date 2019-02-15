package com.example.wangx.chatonly;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Created by wangx on 2018/12/15.
 */

public class UserId extends LitePalSupport{

    public String getUsername() {
        return Username;
    }

    public UserId(String username) {
        Username = username;
    }

    @Column(unique = true, defaultValue = "unknown")
    private String Username;

}
