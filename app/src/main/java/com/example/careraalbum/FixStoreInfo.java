package com.example.careraalbum;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;


public class FixStoreInfo extends AppCompatActivity {
    private final int CALL=1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        //不使用原生标题栏
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.fix_store_info);

        //获取控件实例
        Button goHome=(Button)findViewById(R.id.back_to_home_1);
        TextView head=(TextView)findViewById(R.id.head);
        head.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        Button call=(Button)findViewById(R.id.call);
        WebView webView=(WebView)findViewById(R.id.map_view);
        //维修点信息有待居中显示

        //返回主界面
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FixStoreInfo.this,MainActivity.class);
                startActivity(intent);
            }
        });

        //拨打电话
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_DIAL);
                startActivityForResult(intent,CALL);
            }
        });

        //webView加载高德地图网页版首页
        webView.getSettings().setJavaScriptEnabled(true);//支持js脚本
        webView.setWebViewClient(new WebViewClient());//只在webview控件内加载网页
        webView.loadUrl("https://m.amap.com/around/?locations=116.3152,39.959985&keywords=4s,汽修&"
                +"defaultIndex=1&defaultView=list&searchRadius=5000&key=e97aad32b5973220e0a739c519971d10");
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case CALL:{
                if(resultCode==RESULT_CANCELED){
                    Intent intent=new Intent(FixStoreInfo.this,WhetherFixFinished.class);
                    startActivity(intent);
                }
                break;
            }
            default:break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
