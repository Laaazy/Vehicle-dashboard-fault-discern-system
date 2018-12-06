package com.example.careraalbum;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class FindFixStore extends AppCompatActivity {
    //活动构造函数
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        //不使用原生标题栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.show_problem_info);

        //获取控件实例
        ImageView picture=(ImageView)findViewById(R.id.picture_show_2);
        TextView problem_type=(TextView)findViewById(R.id.problem_type);
        TextView problem_type_info=(TextView)findViewById(R.id.problem_type_info);
        TextView problem_reason=(TextView)findViewById(R.id.problem_reason) ;
        TextView problem_reason_info=(TextView)findViewById(R.id.problem_reason_info);
        Button find_fix_store=(Button)findViewById(R.id.find_fix_store);

        //标题加粗
        problem_type.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        problem_reason.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        //获取启动该活动的intent
        final Intent intent=getIntent();
        problem_type_info.setText(intent.getStringExtra("problem_type"));
        problem_type_info.setGravity(Gravity.CENTER);
        problem_reason_info.setText(intent.getStringExtra("problem_reason"));
        problem_reason_info.setGravity(Gravity.CENTER);
        picture.setImageURI(Uri.parse(intent.getStringExtra("image_uri")));



        //查找维修点按钮的点击逻辑
        find_fix_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FindFixStore.this,FixStoreInfo.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

}
