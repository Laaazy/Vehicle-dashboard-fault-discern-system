package com.example.careraalbum;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.Manifest;

public class MainActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int CROP_PICTURE = 3;
    private Uri imageUri;

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
        setContentView(R.layout.activity_main);

        //获取控件实例
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);


        //拍摄照片
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                //创建文件实体
                try {
                    if (outputImage.exists())
                        outputImage.delete();
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //获得文件路径，当安卓版本6.0以上时需要使用FileProvider来获取路径否则将被认为不安全
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.cameraalbum.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }

                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        //从相册选择照片
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //若未取得读相册权限，先取得权限
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });

        //String cascade=getResources().getXml(R.xml.cascade).getName();
    }

    //请求许可结果
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(this, "您拒绝了应用读取相册的权限", Toast.LENGTH_SHORT).show();
                break;
            }
            default:
        }
    }

    //打开相册
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //拍摄照片
            case TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    startPhotoCrop(imageUri);
                }
                break;
            }
            //从相册选取照片
            case CHOOSE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    //拿到被选择图片的uri
                    imageUri = data.getData();
                    startPhotoCrop(imageUri);
                }
                break;
            }
            //裁剪照片
            case CROP_PICTURE: {
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(MainActivity.this, CheckProblems.class);
                    intent.putExtra("image_uri", imageUri.toString());
                    startActivity(intent);
                }
                break;
            }
            default:
                break;
        }
    }

    public void startPhotoCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);


        intent.putExtra("return-data", false);
        //指定输出文件名及输出路径
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        //获得输出文件路径，当安卓版本6.0以上时需要使用FileProvider来获取路径否则将被认为不安全
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(MainActivity.this, "com.example.cameraalbum.fileprovider", outputImage);
        }else {
            uri = Uri.fromFile(outputImage);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        imageUri = uri;
        startActivityForResult(intent, CROP_PICTURE);
    }

    //back键的点击事件处理,在mainActivity活动按下back键退出应用
    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}


