package com.example.wangx.chatonly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.wangx.chatonly.ada.ChatOnlySplashActivity;
import com.example.wangx.chatonly.util.LogUtil;

public class WelcomeActivity extends AppCompatActivity {
    private static final int DELAY = 3000;
    private static final int GO_GUIDE = 0;
    private static final int GO_HOME = 1;
    private LogUtil Log = new LogUtil();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initLoad();
    }

    private void initLoad() {
        SharedPreferences sharedPreferences = getSharedPreferences("chatonly", MODE_PRIVATE);
        boolean guide = sharedPreferences.getBoolean("guide", true);
        if (!guide) {
            handler.sendEmptyMessageDelayed(GO_HOME, DELAY);
        } else {
            handler.sendEmptyMessageDelayed(GO_GUIDE, DELAY);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("guide",false);
            editor.apply();
        }
    }

    private void goHome() {
        Intent intent = new Intent(this, ChatOnlySplashActivity.class);
        startActivity(intent);
        finish();
    }

    private void goGuide() {
        Intent intent = new Intent(this, GuideActivity.class);
        startActivity(intent);
        finish();//结束当前活动
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_GUIDE: {
                    goGuide();
                    break;
                }
                case GO_HOME: {
                    goHome();
                    break;
                }
                default:
                    break;
            }
        }
    };
}
