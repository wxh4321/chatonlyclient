package com.example.wangx.chatonly;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.wangx.chatonly.server.DataBaseManager;
import com.example.wangx.chatonly.server.SecuityManager;
import com.example.wangx.chatonly.server.ServerManager;
import com.example.wangx.chatonly.util.LogUtil;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ImageView imageView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String username_send,chatobj,label;
    private String address,chatobj_address;
    private ServerManager serverManager = ServerManager.getServerManager();
    private SecuityManager secuityManager = new SecuityManager();
    private DataBaseManager dataBaseManager = new DataBaseManager();
    private LogUtil Log = new LogUtil();
    private int[] imageIds = {
            R.drawable.boy1,R.drawable.girl1,  R.drawable.boy7,R.drawable.girl7,
            R.drawable.boy2,R.drawable.girl3,  R.drawable.boy8,R.drawable.girl8,
            R.drawable.boy3,R.drawable.girl2,  R.drawable.boy9,R.drawable.girl9,
            R.drawable.boy4,R.drawable.girl4,  R.drawable.boy10,R.drawable.girl10,
            R.drawable.boy5,R.drawable.girl5,  R.drawable.boy11,R.drawable.girl11,
            R.drawable.boy6,R.drawable.girl6

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverManager.start();
        username_send = dataBaseManager.findUserName();
        if (username_send.equals("")||username_send.equals(null)){
            username_send = GenerateRandId();
            UserId userId = new UserId(username_send);
            userId.save();//保存用户名到数据库
        }
        Log.d("username",username_send);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        navigationView.setCheckedItem(R.id.nav_friends);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.nav_location:
                        Intent intent = new Intent(MainActivity.this,LBSActivity.class);
                        intent.putExtra("username_send", username_send);
                        startActivity(intent);
                        break;
                    case R.id.nav_friends:
                        if (login(null)){
                            Intent intent2 = new Intent(MainActivity.this,ChatActivity.class);
                            intent2.putExtra("chatobj",chatobj);
                            intent2.putExtra("contextname","nav_friends");
                            startActivity(intent2);
                        }
                        else {
                            imageView.setImageResource(R.drawable.nopeople);//显示没有联系人
                        }
                        break;
                    case R.id.nav_mail://联系开发者
                        if (login("admin")){
                            Intent intent3 = new Intent(MainActivity.this,ChatWithAdminActivity.class);
                            intent3.putExtra("chatobj",chatobj);
                            intent3.putExtra("contextname","nav_mail");
                            startActivity(intent3);
                        }
                        else {
                            imageView.setImageResource(R.drawable.mywechat);//显示没有联系人
                        }
                        break;
                    case R.id.nav_tasks://吐槽与广告
                        Intent intent4 = new Intent(MainActivity.this,ShowMessagesActivity.class);
                        startActivity(intent4);
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        imageView = (ImageView)findViewById(R.id.pic_image);
        imageView.setImageResource(generateImageId());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.baidu.com"));
                startActivity(intent);
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPic();
            }
        });
    }

private void  refreshPic(){
    new Thread(new Runnable() {
        @Override
        public void run() {
            try{
                Thread.sleep(2000);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(generateImageId());
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }).start();
}
    private int generateImageId(){
            Random random = new Random();
            int index = random.nextInt(imageIds.length);
            return imageIds[index];
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
//                Toast.makeText(this,"You clicked Open",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }


    private boolean login(String Is_admin) {
        address = dataBaseManager.find_address();
        Log.d("username","address"+address);
        if (address.equals("")){
            address = "null,null";
        }
        String msg;
        String index = String.valueOf(username_send.length());
        if (Is_admin==null){
            // send msg to servers
            msg = "[LOGIN]:[" + username_send +","+ address + "]";
        }
        else {
            index = "5";
            if(address.equals("null,null")){
                address = "notnull,notnull";
            }
            msg = "[LOGIN]:[" + (Is_admin+username_send) +","+ address + "]";
        }
        //加密
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
            Log.d("username","label : "+label);
            Log.d("username","chatobj : "+chatobj);
            Log.d("username","chatobj_address : "+chatobj_address);
        }
        if (chatobj.equals("nobody")){
            return false;
        }
        return label.equals("1")||label.equals("2");
    }
    private String GenerateRandId(){
        Random random = new Random();
        char [] c = new char[26];
        int i,i1,i2;
        int j = (int)'a';
        for(i = j;i<j+26;i++){
            c[i-j] = (char)(i);
        }
        String a = "0123456789";
        char[] num = a.toCharArray();
        String randNum = "";
        for(int k =0;k<6;k++){
            i1 = random.nextInt(26);
            i2 = random.nextInt(10);
            randNum += c[i1];
            randNum+=num[i2];
        }
        String s = currentTime();
        return randNum+s;
    }
    private String currentTime()
    {
        Date date = new Date();
        return toString(date);
    }
    // 日期转化为字符串形式
    private String toString(Date date)
    {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(date);
    }
}
