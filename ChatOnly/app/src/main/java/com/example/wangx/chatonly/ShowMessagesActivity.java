package com.example.wangx.chatonly;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangx.chatonly.server.DataBaseManager;
import com.example.wangx.chatonly.server.SecuityManager;
import com.example.wangx.chatonly.server.ServerManager;
import com.example.wangx.chatonly.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowMessagesActivity extends AppCompatActivity {
    private MyThread thread;
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private MyAdapter myAdapter;
    private Handler handler;
    private ServerManager serverManager = ServerManager.getServerManager();
    private SecuityManager secuityManager = new SecuityManager();
    private DataBaseManager dataBaseManager = new DataBaseManager();
    private LogUtil Log = new LogUtil();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);
        if(!find_UserMessage()){
            Toast.makeText(this,"没有留言信息",Toast.LENGTH_SHORT).show();
        }
        msgRecyclerView = (RecyclerView)findViewById(R.id.msg1_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter(msgList);
        msgRecyclerView.setAdapter(myAdapter);
        msgRecyclerView.scrollToPosition(msgList.size()-1);//将RecyclerVIEW定位到最后一行
        Toolbar toolbar = (Toolbar)findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                try{
                    String message = serverManager.getMessage1();
                    if (!message.equals(null)){
                        delChatMsg(message);
                        myAdapter = new MyAdapter(msgList);
                        msgRecyclerView.setAdapter(myAdapter);
                        msgRecyclerView.scrollToPosition(msgList.size()-1);//将RecyclerVIEW定位到最后一行
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread=new MyThread();
        thread.start();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
    private boolean find_UserMessage(){
        String username = dataBaseManager.findUserName();
        if (username.equals("")){
           return false;
        }
        String label = "";
        // send msg to servers
        String msg = "[GETMESSAGE]:[" + username +"]";
        //加密
        String index = String.valueOf(5);
        msg = secuityManager.Encryption(msg,index);
        serverManager.setMessage(null,msg,null);
        serverManager.sendMessage();
        // get msg from servers return
        String ack = serverManager.getMessage();
        //解密
        ack = secuityManager.Deciphering(ack,index);
        // deal msg
        if (ack == null||ack=="") {
            return false;
        }
        serverManager.setMessage(null,null,null);
        String p = "\\[ACKGETMESSAGE\\]:\\[(.*)\\]";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(ack);
        if (matcher.find()){
            label = matcher.group(1);
        }
        return label.equals("3");
    }
    class MyThread extends Thread{
        @Override
        public void run() {
            while(true){
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!thread.isAlive()) {//没有启动重新启动
                    thread.start();
                }
            }
        }
    }

    private void delChatMsg(String msg) {
        String time = null;
        String content = null;
        String avatarID = null;
        String fileType = null;
        String group = null;
        //解密
        String index = String.valueOf(5);
        msg = secuityManager.Deciphering(msg,index);
        serverManager.setMessage(null,null,null);
        String p = "\\[GETUSERMESSAGE\\]:\\[(.*),(.*)\\]";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            time = matcher.group(1);
            content = matcher.group(2);
            Msg chatMsg = new Msg(msgList.size(),content,Msg.TYPE_RECEIVED,time);
            msgList.add(chatMsg);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>  {
        private List<Msg> mMsgList;
        class ViewHolder extends RecyclerView.ViewHolder{
            LinearLayout messagelayout;
            LinearLayout timelayout;
            TextView leftMsg;
            TextView time;


            public ViewHolder(View view){
                super(view);
                messagelayout = (LinearLayout)view.findViewById(R.id.message_layout);
                timelayout = (LinearLayout) view.findViewById(R.id.time_layout);
                leftMsg = (TextView)view.findViewById(R.id.message_info);
                time = (TextView)view.findViewById(R.id.time_info);
            }
        }
        public MyAdapter(List<Msg> msgList){
            mMsgList = msgList;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_message_item,parent,false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            Msg msg = mMsgList.get(position);
            if (msg.getType()==Msg.TYPE_RECEIVED)
            {
                holder.messagelayout.setVisibility(View.VISIBLE);
                holder.timelayout.setVisibility(View.VISIBLE);
                holder.leftMsg.setText(msg.getContent());
                holder.time.setText(msg.getUsername());
            }
        }
        @Override
        public int getItemCount() {
            return mMsgList.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
