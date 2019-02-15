package com.example.wangx.chatonly.server;

import android.content.Context;
import android.util.Log;

import com.example.wangx.chatonly.util.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by wangx on 2018/12/31.
 */

public class ServerManager extends Thread{
    private static final String IP = "114.115.200.46";
    private Socket socket;
    private String username;
    private int iconID;
    private String message = "";
    private Context context;
    private String msg;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private static final ServerManager serverManager = new ServerManager();
    private LogUtil Log = new LogUtil();
    public static ServerManager getServerManager() {
        return serverManager;
    }

    private ServerManager()
    {

    }

    public void run() {
        try {
            Log.d("username", "i hava runned!");
            socket = new Socket(IP, 37777);
            bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String line,m="";
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.equals("-1")) {
                    m += line;
                    Log.d("username", "line : " + m);
                }
                else {
                    Log.d("username", "receive : " + m);
                    message = m;
                    m = "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                bufferedReader.close();
                bufferedWriter.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    while (socket == null) ;
                    if (bufferedWriter != null) {
                        Log.d("username","send : " + msg);
                        bufferedWriter.write(msg + "\n");//Windows的换行是"\r\n",Linux的则是"\n"
                        bufferedWriter.flush();
                        bufferedWriter.write("-1\n");
                        bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public void setMessage(Context context,String msg,String message){
        Log.d("username","setmessage");
        this.context = context;
        this.msg = msg;
        this.message = message;
    }
    public String getMessage() {
        Log.d("username","message : "+message);
        for (int i = 0; i < 5; i++) {
            if (message != null) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return message;
    }
    public String getMessage1() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }
}
