package com.example.wangx.chatonly;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.wangx.chatonly.ada.ChatOnlySplashActivity;
import com.example.wangx.chatonly.adapter.AdapterGuideViewPager;
import com.example.wangx.chatonly.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private AdapterGuideViewPager adapterGuideViewPager;
    private List<View> viewList;
    private ImageView imageViews[] = new ImageView[4];
    private int[] indicatorDotIds = {R.id.iv_indicator_dot1, R.id.iv_indicator_dot2, R.id.iv_indicator_dot3,R.id.iv_indicator_dot4};
    private Button btnToMain;
    private LogUtil Log = new LogUtil();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide);
        initViews();
    }

    @SuppressLint("InflateParams")
    private void initViews() {
        // load view
        final LayoutInflater inflater = LayoutInflater.from(this);

        viewList = new ArrayList<>();
        viewList.add(inflater.inflate(R.layout.guide_page4, null));
        viewList.add(inflater.inflate(R.layout.guide_page1, null));
        viewList.add(inflater.inflate(R.layout.guide_page2, null));
        viewList.add(inflater.inflate(R.layout.guide_page3, null));

        // bind Id with imageView
        for (int i = 0; i < indicatorDotIds.length; i++) {
            imageViews[i] = (ImageView) findViewById(indicatorDotIds[i]);
        }

        adapterGuideViewPager = new AdapterGuideViewPager(this, viewList);

        viewPager = (ViewPager) findViewById(R.id.vp_guide);
        viewPager.setAdapter(adapterGuideViewPager);
        viewPager.addOnPageChangeListener(this);

        btnToMain = (Button) (viewList.get(3)).findViewById(R.id.btn_to_main);
        btnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuideActivity.this, ChatOnlySplashActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * change the indicator when page changed
     * @param position current page ID
     */
    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < indicatorDotIds.length; i++) {
            if (i != position) {
                imageViews[i].setImageResource(R.drawable.unselected);
            } else {
                imageViews[i].setImageResource(R.drawable.selected);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
