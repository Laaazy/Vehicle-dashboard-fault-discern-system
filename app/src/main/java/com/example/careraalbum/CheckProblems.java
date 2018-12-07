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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class CheckProblems extends AppCompatActivity {
    //裁剪后的图片的uri
    private Uri imageUri;
    //将用于后续图像处理的bitmap
    private Bitmap bitmap=null;
    //显示图片的imageView
    private ImageView picture;
    //返回检测结果的数组
    private String[] results;
    //返回结果results中包含的条目数，偶数为故障类型，奇数为故障原因，最终条目数为results条
    int resultsNum=0;


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

                //搜索故障类型
                String problem_type="";
                for(int i=0;i<resultsNum;i++){
                    if(i%2==0)
                    problem_type+=results[i];
                }
                //搜索故障原因
                String problem_reason="";
                for(int i=0;i<resultsNum;i++) {
                    if(i%2!=0) {
                        problem_reason+=results[i-1];
                        problem_reason += results[i];
                    }
                }
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

    private void prepareCascade(int i){
        //首先读取文件，解析为string
        String content="";
        String line="";
        InputStream ins=null;
        if(i==1)
            ins= this.getResources().openRawResource(R.raw.cascade1);
        else if(i==2)
            ins= this.getResources().openRawResource(R.raw.cascade2);
        else if(i==3)
            ins= this.getResources().openRawResource(R.raw.cascade3);
        else if(i==4)
            ins= this.getResources().openRawResource(R.raw.cascade4);
        else if(i==5)
            ins= this.getResources().openRawResource(R.raw.cascade5);
        else if(i==6)
            ins= this.getResources().openRawResource(R.raw.cascade6);
        else if(i==7)
            ins= this.getResources().openRawResource(R.raw.cascade7);
        else if(i==8)
            ins= this.getResources().openRawResource(R.raw.cascade8);
        else if(i==9)
            ins= this.getResources().openRawResource(R.raw.cascade9);
        else if(i==10)
            ins= this.getResources().openRawResource(R.raw.cascade10);
        else if(i==11)
            ins= this.getResources().openRawResource(R.raw.cascade11);
        else if(i==12)
            ins= this.getResources().openRawResource(R.raw.cascade12);
        InputStreamReader reader= null;
        try {
            reader = new InputStreamReader(ins,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader=new BufferedReader(reader);
        try {
            while((line=bufferedReader.readLine())!=null){
                content+=line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //然后将string作为文件存储到cache目录下
        File cascade=new File(getExternalCacheDir(),"cascade"+i+".xml");
        if(!cascade.exists()) {
            try {
                FileOutputStream fos =new FileOutputStream(cascade);
                fos.write(content.getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Mat dobj(Mat src){
        Mat dst=src.clone();
        for(int j=1;j<=12;j++) {
            //识别出一次之后就不再识别
            int flag=0;
            dst=src.clone();
            //准备好cascadej.xml
            prepareCascade(j);
            String path = getExternalCacheDir() + "/cascade"+j+".xml";
            CascadeClassifier objDetector = new CascadeClassifier(path);

            MatOfRect objDetections = new MatOfRect();

            objDetector.detectMultiScale(dst, objDetections);

            //不符合分类器对应分类
            if (objDetections.toArray().length <= 0) {
                continue;
            }
            for (Rect rect : objDetections.toArray()) {
                if (flag==1)
                    break;
                flag=1;
                Imgproc.rectangle(dst, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
                if(j==1) {
                    results[resultsNum++] = "发动机故障！\n";
                    results[resultsNum++] = "1. 汽油品质差，不达标。\n" +
                            "2. 氧传感器故障。\n" +
                            "3. 空气流量传感器故障。\n" +
                            "4. 火花塞积碳。\n" +
                            "5. 发动机爆震。\n" +
                            "6. 水温传感器损坏。\n" +
                            "7. 发动机缺缸。\n" +
                            "...等等，污染灯亮有很多原因导致，但导致最多的是前三项。\n";
                }
                else if(j==2) {
                    results[resultsNum++] = "车身稳定系统故障！\n";
                    results[resultsNum++] = "1. ABS泵故障及ABS泵供电搭铁线路故障。\n" +
                            "2. 传感器接线头松脱或某个传感器功能失效。\n" +
                            "3. 相关保险丝熔断或烧蚀。\n" +
                            "4. 系统信号传播受阻。\n" +
                            "5. 假故障，系统误报，尝试按下关闭按钮进行重启。\n";
                }
                else if(j==3) {
                    results[resultsNum++] = "灯泡故障！\n";
                    results[resultsNum++] = "1. 有灯泡不能正常工作或者损坏。\n" +
                            "2. 指示灯故障。\n";
                }
                else if(j==4) {
                    results[resultsNum++] = "发动机电子稳定系统故障！\n";
                    results[resultsNum++] = "1. 发动机故障。\n" +
                            "2. 电子稳定系统故障。\n" +
                            "3. 指示灯故障。\n" +
                            "4. 传感器故障。\n" +
                            "请分别排查各类型故障，详请参见给类型故障原因";
                }
                else if(j==5) {
                    results[resultsNum++] = "机油油位过低！\n";
                    results[resultsNum++] = "1. 机油油管漏油，机油泵损坏或其零部件磨损超标。\n" +
                            "2. 机油稀或因发动机温度高造成机油变稀,进而机油泄露。\n" +
                            "3. 曲轴与大小瓦之间的间隙超标导致机油泄漏。\n" +
                            "4. 限压阀或泄压阀弹簧过软，发卡或钢珠损伤造成阀的功能消失或减弱导致机油压力降低。\n" +
                            "5. 机油脏或粘稠导致机油泵不能将机油有效吸入、泵出。\n" +
                            "6. 机油稀或因发动机温度高造成机油变稀,进而机油泄露。\n";
                }
                else if(j==6) {
                    results[resultsNum++] = "轮胎压力故障！\n";
                    results[resultsNum++] = "1. 轮胎气压不足。\n" +
                            "2. 轮胎出现破损情况。\n" +
                            "3. 被扎导致漏气。\n" +
                            "4. 胎压传感器故障。\n";
                }
                else if(j==7) {
                    results[resultsNum++] = "前部安全带故障！\n";
                    results[resultsNum++] = "1. 前排两个座椅安全带没系。\n" +
                            "2. 传感器故障故障。\n";
                }
                else if(j==8) {
                    results[resultsNum++] = "水温警报！\n";
                    results[resultsNum++] = "1. 水温过高。\n" +
                            "2. 水量过低。\n" +
                            "3. 传感器故障。\n";
                }
                else if(j==9) {
                    results[resultsNum++] = "蓄电池故障！\n";
                    results[resultsNum++] = "1. 电池电量不够。\n" +
                            "2. 电池故障。\n" +
                            "3. 指示灯故障。\n";
                }
                else if(j==10) {
                    results[resultsNum++] = "制动防抱死系统故障！\n";
                    results[resultsNum++] = "1. 制动防抱死系统故障。\n" +
                            "2. 传感器故障。\n";
                }
                else if(j==11) {
                    results[resultsNum++] = "制动装置故障！\n";
                    results[resultsNum++] = "1. 制动装置指示灯故障。\n" +
                            "2. 传感器故障。\n";
                }
                else if(j==12) {
                    results[resultsNum++] = "驻车指示灯故障！\n";
                    results[resultsNum++] = "1. 电子驻车。\n" +
                            "2. 指示灯故障。\n";
                }
            }
        }
        return dst;
    }


}
