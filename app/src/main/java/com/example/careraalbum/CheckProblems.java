package com.example.careraalbum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class CheckProblems extends AppCompatActivity {
    //裁剪后的图片的uri
    private Uri imageUri;
    //将用于后续图像处理的bitmap
    private Bitmap bitmap=null;


    //活动构造函数
    @Override
    protected void onCreate(Bundle savedInstanceState){
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
        setContentView(R.layout.show_pic_after_crop);

        //获取控件实例
        Button check=(Button)findViewById(R.id.find_problem);
        ImageView picture=(ImageView)findViewById(R.id.picture_show_1);
        //获取启动该活动的intent
        Intent intent=getIntent();

        imageUri=Uri.parse(intent.getStringExtra("image_uri"));
        try {
            //显示裁剪后的图片
            picture.setImageURI(imageUri);
            //将裁剪后的照片转化为bitmap
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
        }catch (Exception e){
            e.printStackTrace();
        }

        //在这里调用图像处理算法处理bitmap,然后将得出的故障信息传递给下一个活动
        check.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //image handle here...use saved bitmap...get problem_type and problem_reason...
                //故障类型
                String problem_type=new String("这是故障类型");
                //故障原因
                String problem_reason=new String("这是故障原因");
                Intent intent=new Intent(CheckProblems.this,FindFixStore.class);
                intent.putExtra("problem_type",problem_type);
                intent.putExtra("problem_reason",problem_reason);
                intent.putExtra("image_uri",imageUri.toString());
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
