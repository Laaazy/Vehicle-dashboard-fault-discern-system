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
import android.widget.Toast;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;



public class CheckProblems extends AppCompatActivity {
    //裁剪后的图片的uri
    private Uri imageUri;
    //将用于后续图像处理的bitmap
    private Bitmap bitmap=null;
    //显示图片的imageView
    private ImageView picture;
    //返回检测结果的数组
    private String[] results;


    //活动构造函数
    @Override
    protected void onCreate(Bundle savedInstanceState){
        results=new String[10];
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
        picture=(ImageView)findViewById(R.id.picture_show_1);
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
                try{
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                    //bitmap转mat
                    //或者imageUri.toString()
                    String path= getExternalCacheDir()+"/output_image.jpg";
                    Mat src=Imgcodecs.imread(path);
                    if(src.empty()){
                        Toast.makeText(CheckProblems.this,"file not found",Toast.LENGTH_SHORT).show();
                        throw new Exception("no file");
                    }
                    Mat dst=dobj(src);
                }catch(Exception e){
                    Toast.makeText(CheckProblems.this,"故障未被成功检测",Toast.LENGTH_SHORT).show();
                    //System.out.println("例外："+e);
                }

                //故障类型
                String problem_type=results[0];
                //故障原因
                String problem_reason=results[1];
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

    private Mat dobj(Mat src){
        int i=0;
        Mat dst=src.clone();

        String path= getExternalCacheDir()+"/cascade.xml";
        CascadeClassifier objDetector=new CascadeClassifier(path);

        MatOfRect objDetections=new MatOfRect();

        objDetector.detectMultiScale(dst, objDetections);

        if(objDetections.toArray().length<=0){
            return src;
        }
        for(Rect rect:objDetections.toArray()){
            Imgproc.rectangle(dst, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0,0,255),2);
            results[i++]="发动机故障！";
            results[i++]="1. 汽油品质不好会导致混合气在气缸内燃烧不充分导致污染灯亮，而且还容易产生积碳。\n" +
                    "2. 进气道，活塞顶端积碳存在会导致雾化不良从而引起燃烧不充分导致污染灯亮。\n" +
                    "3. 冷车启动时，特别是天气温度急剧下降时，由于电脑的温度修正问题启动时会导致污染灯亮，但只要温度下降到一定温度不起伏变化后就会相对稳定。\n" +
                    "4. 发动机行驶里程过长，火花塞工作特性减弱会导致污染灯点亮。\n" +
                    "5. 积碳过多点火困难会导致点火线圈反向击穿导致点火线圈故障引起点火不良导致燃烧不完全产生的污染灯亮，这种现象有个鲜明的特征就是当点火线圈故障时发动机是始终严重的抖动。如果不是始终的严重抖动那点火线圈基本可以排除。\n" +
                    "6. 车辆车龄过大机械老化导致发动机工况不良导致燃烧点火不良导致污染灯亮。\n" +
                    "7. 部分传感器电脑板本身故障导致污染灯亮。\n" +
                    "...等等，污染灯亮有很多原因导致，但导致最多的是前三项。\n"
                    + "特别提示： 当污染灯亮时，发动机动作不抖或有点轻微抖动，此时车主在使用时无需担心，可以正常使用，有时间或方便时可以来检查或保养。";
        }
        return dst;
    }

}
