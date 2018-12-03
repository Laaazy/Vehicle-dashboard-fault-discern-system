package com.example.careraalbum;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.security.PublicKey;
import java.util.List;


public class FixStoreInfo extends AppCompatActivity implements PoiSearch.OnPoiSearchListener {
    private final int CALL=1;
    private static int flag=0;//标志，当定位结果从服务器返回时置为1
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private static LatLonPoint startPoint=new LatLonPoint(0,0);//搜索起点
    //搜索矩形,参数为搜索起点及搜索范围
    private PoiSearch.SearchBound searchBound=null;
    private String fixStoreName=new String("");//维修点名称
    private String fixStoreAddress=new String("");//维修点地址
    private String fixStoreTel=new String("");//维修点电话
    private String fixStoreDis=new String("");//维修点距离
    public static TextView fixStoreInfo;
    public static String[] tels;

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
        fixStoreInfo=(TextView)findViewById(R.id.fix_store_info);
        Button call=(Button)findViewById(R.id.call);
        WebView webView=(WebView)findViewById(R.id.map_view);


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
                if(tels[0]!=null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tels[0]));
                    startActivityForResult(intent, CALL);
                }
                else{
                    Toast.makeText(FixStoreInfo.this,"请先查询维修点",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //使用定位SDK先获取起点坐标,为startPoint赋值
        getLocation();

        //等待服务器返回定位结果
        try {
            Thread.currentThread();
            Thread.sleep(3000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }



        //webView加载高德地图网页版
        webView.getSettings().setJavaScriptEnabled(true);//支持js脚本
        webView.setWebViewClient(new WebViewClient());//只在webview控件内加载网页
        webView.loadUrl("https://m.amap.com/around/?locations="+String.valueOf(startPoint.getLongitude()) +","
                +String.valueOf(startPoint.getLatitude())+"&keywords=汽修,4S&"
                +"defaultIndex=1&defaultView=list&searchRadius=5000&key=e97aad32b5973220e0a739c519971d10");

        //开始查询POI
        doSearchQuery();
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

    protected void doSearchQuery(){
        currentPage=0;
        //设置查询POI代码，030000代表汽车维系
        query=new PoiSearch.Query("","030000","");
        //设置返回结果按照距离排序
        query.setDistanceSort(true);
        //每页查10个POI
        query.setPageSize(10);
        //设置当前页,查第0页
        query.setPageNum(currentPage);

        poiSearch=new PoiSearch(this,query);
        if(0==startPoint.getLongitude()&&0==startPoint.getLatitude())
           Toast.makeText(FixStoreInfo.this,"起始点异常为空",Toast.LENGTH_SHORT).show();
        //搜索矩形,参数为搜索起点及搜索范围
        searchBound=new PoiSearch.SearchBound(startPoint,5000);
        //设置矩形查询范围
        poiSearch.setBound(searchBound);
        //设置监听器，返回结果时触发回调
        poiSearch.setOnPoiSearchListener(this);
        //发出请求，开始查询符合条件的POI
        poiSearch.searchPOIAsyn();
    }

    //POI信息查询回调方法
    @Override
    public void onPoiSearched(PoiResult result,int rCode){
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条POI查询
                    poiResult = result;
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    //yes
                    if (poiItems != null && poiItems.size() > 0) {
                       //开始解析维修点信息，显示到布局的维修点信息TextView上
                        //维修点名称
                        fixStoreName=poiItems.get(0).getTitle();
                        //维修点地址
                        fixStoreAddress=poiItems.get(0).getSnippet();
                        //维修点电话
                        fixStoreTel=poiItems.get(0).getTel();
                        tels=fixStoreTel.split(";");
                        //维修点距离
                        fixStoreDis=String.valueOf(poiItems.get(0).getDistance());

                        FixStoreInfo.fixStoreInfo.setText("维修点名称:"+fixStoreName+"\n"
                                +"维修点地址:"+fixStoreAddress+"\n"
                                +"维修点距离:"+fixStoreDis+"米\n"
                                +"维修点电话:\n");
                        for( String s:tels){
                            FixStoreInfo.fixStoreInfo.append(s+"\n");
                        }
                    } else {
                        Toast.makeText(FixStoreInfo.this,"未查询到维修点",Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(FixStoreInfo.this,"返回结果为空或查询条件异常",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,String.valueOf(rCode),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem item,int rCode){
        //此处无显示POI item的界面，do nothing
    }

    //调用高德定位SDK获取当前位置，为startPoint赋值
    public void getLocation(){
        AMapLocationClient mapLocationClient=new AMapLocationClient(this.getApplicationContext());
        AMapLocationListener mapLocationListener=new AMapLocationListener() {
            //监听Location的回调函数,在其中解析坐标信息
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if(null!=aMapLocation){
                    if(aMapLocation.getErrorCode()==0){
                        //获得纬度
                        startPoint.setLatitude(aMapLocation.getLatitude());
                        //获得经度
                        startPoint.setLongitude(aMapLocation.getLongitude());
                        Toast.makeText(FixStoreInfo.this,"经度:"+String.valueOf(aMapLocation.getLongitude())
                                +"纬度:"+String.valueOf(aMapLocation.getLatitude()),Toast.LENGTH_LONG);
                    }else{
                        Toast.makeText(FixStoreInfo.this,"定位当前位置失败",Toast.LENGTH_SHORT).show();
                    }
                }
                //服务器返回结果完成,flag置为1
                flag=1;
            }
        };
        //设置Location监听器
        mapLocationClient.setLocationListener(mapLocationListener);

        //在其中配置定位功能选项
        AMapLocationClientOption option=new AMapLocationClientOption();
        //只进行一次定位
        option.setOnceLocation(true);
        //使用最近3秒内精度最高的一次定位
        //option.setOnceLocationLatest(true);
        //设置网络请求超时时间,10s
        //option.setHttpTimeOut(10000);
        //开始定位
        if(null!=mapLocationClient){
            mapLocationClient.setLocationOption(option);
            mapLocationClient.stopLocation();
            mapLocationClient.startLocation();
        }
    }
}
