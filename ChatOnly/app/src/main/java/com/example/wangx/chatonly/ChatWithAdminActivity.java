package com.example.wangx.chatonly;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangx.chatonly.server.DataBaseManager;
import com.example.wangx.chatonly.server.SecuityManager;
import com.example.wangx.chatonly.server.ServerManager;
import com.example.wangx.chatonly.util.LogUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatWithAdminActivity extends AppCompatActivity {
    private static final int PUSH_NOTIFICATION_ID = (0x001);
    private static final String PUSH_CHANNEL_ID = "PUSH_NOTIFY_ID";
    private static final String PUSH_CHANNEL_NAME = "PUSH_NOTIFY_NAME";
    private MyThread thread;
    private GetActivityThread getActivityThread;
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MyAdapter myAdapter;
    private Handler handler;
    private PopupWindow popupWindow;
    private int longClickPosition;
    private  int id;
    private TextView tvDelete;
    private String contextname = null,runningActivity=null;
    private String username_send=null,chatobj=null,label;
    private String address,chatobj_address;
    private boolean userislogin = false;
    private int t=800;
    private ServerManager serverManager = ServerManager.getServerManager();
    private SecuityManager secuityManager = new SecuityManager();
    private DataBaseManager dataBaseManager = new DataBaseManager();
    private LogUtil Log = new LogUtil();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_admin);
        if (contextname==null){//通知登录判断
            if (login()){
                contextname = "create_chatwithadminactivity";
                userislogin = true;
            }
        }
        initMsgs();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        inputText = (EditText)findViewById(R.id.input_text);
        send = (Button)findViewById(R.id.send);
        msgRecyclerView = (RecyclerView)findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter(msgList);
        msgRecyclerView.setAdapter(myAdapter);
        msgRecyclerView.scrollToPosition(msgList.size()-1);//将RecyclerVIEW定位到最后一行
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                try{
                    String message = serverManager.getMessage1();
                    String content_msg = "";
                    if (!message.equals(null)){
                        content_msg = delChatMsg(message);
                        myAdapter = new MyAdapter(msgList);
                        msgRecyclerView.setAdapter(myAdapter);
                        msgRecyclerView.scrollToPosition(msgList.size()-1);//将RecyclerVIEW定位到最后一行
                    }
                    //发送通知
//                    runningActivity = getRunningActivityName();
                    if (!runningActivity.equals("com.example.wangx.chatonly.ChatWithAdminActivity")){
                        if (content_msg!=null){
                            sendNotify(content_msg);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread=new MyThread();
        thread.start();
        getActivityThread = new GetActivityThread();
        getActivityThread.start();
        msgRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean mScrolled = false;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mScrolled){
                    mScrolled = false;
                    t = 800;
                }
                else {
                    mScrolled = true;
                    t = 10000;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(msgList.size(),content,Msg.TYPE_SENT,username_send);
                    if (sendToChatObj(msg.getContent())) {//发送消息成功，做以下操作
                        if (t!=800){
                            t = 800;//设置刷新时间
                        }
                        msgList.add(msg);
                        myAdapter.notifyItemInserted(msgList.size()-1);//当有新消息是刷新RecyclerView中的显示
                        msgRecyclerView.scrollToPosition(msgList.size()-1);//将RecyclerVIEW定位到最后一行
                        inputText.setText("");//清空输入框中的内容
                    }
                    else {
                        Toast.makeText(ChatWithAdminActivity.this, "稍后再试，发送失败，对方可能不在线", Toast.LENGTH_SHORT).show();
                    }
                }
            }}
        );
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
    class GetActivityThread extends  Thread{
        @Override
        public void run() {
            while(true){
                runningActivity = getRunningActivityName();
                try {
                    Thread.sleep(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!getActivityThread.isAlive()) {//没有启动重新启动
                    getActivityThread.start();
                }
            }
        }
    }
    class MyThread extends Thread{
        @Override
        public void run() {
            while(true){
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!thread.isAlive()) {//没有启动重新启动
                    thread.start();
                }
            }
        }
    }

    private boolean sendToChatObj(String content) {
        String label = "";
        if (chatobj==null){
            chatobj = getIntent().getStringExtra("chatobj");
        }
        chatobj = "admin"+chatobj;
        String msg = "[CHATMSG]:[" + chatobj + ", " + content + ", " + "2" +", Text]";//2代表和管理员聊天内容
        //加密
        String index = String.valueOf(5);
        msg = secuityManager.Encryption(msg,index);
        ServerManager.getServerManager().setMessage(this,msg,"");
        ServerManager.getServerManager().sendMessage();
        try {
            Thread.sleep(800);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        String ack = ServerManager.getServerManager().getMessage();
        //解密
        ack = secuityManager.Deciphering(ack,index);
        if (ack == null) {
            return false;
        }
        String p = "\\[ACKCHATMSG\\]:\\[(.*)\\]";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(ack);
        if (matcher.find()){
            label = matcher.group(1);
        }
        return  label.equals("2");
    }
    private String delChatMsg(String msg) {
        String sendName = null;
        String content = null;
        String avatarID = null;
        String fileType = null;
        String group = null;
        //解密
        String index = String.valueOf(5);
        msg = secuityManager.Deciphering(msg,index);
        serverManager.setMessage(null,null,null);
        String p = "\\[GETCHATMSG1\\]:\\[(.*), (.*), (.*), (.*)\\]";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            sendName = matcher.group(1);
            content = matcher.group(2);
            avatarID = matcher.group(3);
            fileType = matcher.group(4);
            Msg chatMsg = new Msg(msgList.size(),content,Msg.TYPE_RECEIVED,sendName);
            msgList.add(chatMsg);
        }
        return content;
    }

    private boolean login() {
        address = dataBaseManager.find_address();
        username_send = dataBaseManager.findUserName();
        Log.d("username","address"+address);
        if (address.equals("")){
            address = "null,null";
        }
        // send msg to servers
        String msg = "[LOGIN]:[" + ("admin"+username_send) +","+ address + "]";
        //加密
        String index = String.valueOf(5);
        msg = secuityManager.Encryption(msg,index);
        serverManager.setMessage(null,msg,null);
        serverManager.sendMessage();
        // get msg from servers return
        String ack = serverManager.getMessage();
        //解密
        ack = secuityManager.Deciphering(ack,index);
        Log.d("username","ack : "+String.valueOf(ack));
        // deal msg
        if (ack == null||ack=="") {
            return false;
        }
        serverManager.setMessage(null,null,null);
        String p = "\\[ACKLOGIN\\]:\\[(.*),(.*),(.*),(.*)\\]";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(ack);
        if (matcher.find()){
            label = matcher.group(1);
            chatobj = matcher.group(2);
            chatobj_address = matcher.group(3)+","+matcher.group(4);
        }
        if (chatobj.equals("nobody")){
            return false;
        }
        return label.equals("2");
    }
    private void sendNotify(String content){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PUSH_CHANNEL_ID, PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificationManager manager=null;
        Notification notification=null;
        if (userislogin){
            Intent intent = new Intent(this,ChatWithAdminActivity.class);
            Log.d("username","chatobj in notify : "+chatobj);
            PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
            manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("来自管理员的消息")
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.label_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.label_logo))
                    .setContentIntent(pi)
                    .setChannelId(PUSH_CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .build();
        }
        else {
            manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("notify error")
                    .setContentText("请重启app")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.label_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.label_logo))
                    .setChannelId(PUSH_CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .build();
        }
        manager.notify(PUSH_NOTIFICATION_ID,notification);
    }
    private String getRunningActivityName(){
        ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>  {
        private List<Msg> mMsgList;
        private PopupWindow popupWindow;
        private int longClickPosition;
        private ViewGroup viewGroup;
        private TextView tvDelete;

        class ViewHolder extends RecyclerView.ViewHolder{
            LinearLayout leftlayout;
            LinearLayout rightlayout;
            TextView leftMsg;
            TextView rightMsg;
            ImageView leftPic,rightPic;


            public ViewHolder(View view){
                super(view);
                leftlayout = (LinearLayout)view.findViewById(R.id.left_layout);
                rightlayout = (LinearLayout)view.findViewById(R.id.right_layout);
                leftMsg = (TextView)view.findViewById(R.id.left_msg);
                rightMsg = (TextView)view.findViewById(R.id.right_msg);
                leftPic = (ImageView)view.findViewById(R.id.notyour_image);
                leftPic.setImageResource(R.drawable.ic_admin);
                rightPic = (ImageView)view.findViewById(R.id.your_image);
                rightPic.setImageResource(R.drawable.ic_user);
            }
        }
        public MyAdapter(List<Msg> msgList){
            mMsgList = msgList;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            viewGroup = parent;
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            Msg msg = mMsgList.get(position);
            if (msg.getType()==Msg.TYPE_RECEIVED)
            {
                //如果收到消息显示左边布局隐藏右边布局
                holder.leftlayout.setVisibility(View.VISIBLE);
                holder.rightlayout.setVisibility(View.GONE);
                holder.rightPic.setVisibility(View.GONE);
                holder.leftPic.setVisibility(View.VISIBLE);
                holder.leftMsg.setText(msg.getContent());
            }
            else if(msg.getType()==Msg.TYPE_SENT){
                //如果发出消息则显示有布局，隐藏左边布局
                holder.rightlayout.setVisibility(View.VISIBLE);
                holder.leftlayout.setVisibility(View.GONE);
                holder.rightPic.setVisibility(View.VISIBLE);
                holder.leftPic.setVisibility(View.GONE);
                holder.rightMsg.setText(msg.getContent());
            }
        }
        @Override
        public int getItemCount() {
            return mMsgList.size();
        }
    }

    private void  initMsgs()
    {
        if(contextname==null)
        {
            contextname = getIntent().getStringExtra("contextname");
        }
        if (chatobj==null)
        {
            chatobj = getIntent().getStringExtra("chatobj");
        }
        Msg msg1 = new Msg(0,"你好，欢迎与我交流，如果我不在您的意见会自动保留，请查看吐槽信息！",Msg.TYPE_RECEIVED,chatobj);
        msgList.add(msg1);
    }
}
