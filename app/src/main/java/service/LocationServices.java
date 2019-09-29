package service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by krito on 2019/7/21.
 */

public class LocationServices extends Service {
//    参数?latitude longitude
//    final static String path = "http://192.168.43.6:8080/location/WriteLocation";
    final static String path = "http://192.168.43.211:8080/peeping/location";

    private HashMap<Integer , String> latmap = new HashMap<>( );
    private HashMap<Integer , String> lonmap = new HashMap<>( );

    //定位点信息
    public LatLng latlng;
    private String strLocationProvince;//定位点的省份
    private String strLocationCity;//定位点的城市
    private String strLocationDistrict;//定位点的区县
    private String strLocationStreet;//定位点的街道信息
    private String strLocationStreetNumber;//定位点的街道号码
    private String strLocationAddrStr;//定位点的详细地址(包括国家和以上省市区等信息)
    private LocationClient mLocationClient =null;//定位客户端
    public MyLocationListener mMyLocationListener = new MyLocationListener();
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isStop = false;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(setLocationClientOption());
        mLocationClient.registerLocationListener(mMyLocationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 触发定时器
        if (!isStop) {
            Log.e("tag", "定时器启动");
            startTimer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mLocationClient!=null) {
            mLocationClient.stop();
        }
        super.onDestroy();
        // 停止定时器
        if (isStop) {
            Log.e("tag", "定时器服务停止");
            stopTimer();
        }
    }

    /*
    * POST发送数据线程
    * */
    private void sendPost(){
        //        新建线程发送数据
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
//                    设置请求方式
                    URL url = new URL( path );
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

//                    组合数据
                    String lat = latmap.get( 1 );
                    String lon = lonmap.get( 1 );

//                    经纬度为空不发送数据
                    if ( lat == null || lon == null || lat.equals( "" ) || lon.equals( "" )){
                        Log.e( "tag" , "当前经纬度为null" );
                    }else {
                        try{
                            String sendmsg="lon=" + lon +"&lat=" + lat;
                            byte[] data = sendmsg.getBytes();

                            Log.e( "tag" , "要发送的lat=" + lat + "," + "要发送的lon=" + lon + "数据长度为：" + sendmsg.length() );

                            conn.setRequestMethod("POST");//设置以Post方式提交数据
                            conn.setConnectTimeout(3000);     //设置连接超时时间
                            conn.setDoInput(true);            //打开输入流，以便从服务器获取数据
                            conn.setDoOutput(true);           //打开输出流，以便向服务器提交数据
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            conn.setRequestProperty("Content-Length", data + ""); // 注意是字节长度, 不是字符长度
                            conn.setUseCaches(false); //使用Post方式不能使用缓存

//                    打开输出流要开始写数据
                            OutputStream os = conn.getOutputStream();

//                    开始写
                            os.write( data );
                            os.close();

//                    获取返回的成功代码
                            int code = conn.getResponseCode();
                            Log.e( "tag" , "服务器的状态返回码：" + code );

                            if (code == 200){
//                        打印成功
                                Log.e( "tag" , "发送数据成功，状态码：" + code );
                            }else {
                                Log.e( "tag" , "发送数据失败，状态码：" + code );
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                            Log.e( "tag", "IO异常" );
                        }
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e( "tag" , "URL错误" );
                }catch (IOException e ) {
                    e.printStackTrace();
                    Log.e( "tag", "IO异常" );
                }
            }
        } ).start();
    }

    /**
     * 定时器 每隔一段时间执行一次
     */
    private void startTimer() {
        isStop = true;//定时器启动后，修改标识，关闭定时器的开关
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {

                @Override
                public void run() {
                    do {
                        try {
                            Log.e("tag", "isStop="+isStop);
                            Log.e("tag", "mMyLocationListener="+mMyLocationListener);
                            mLocationClient.start();
                            Log.e("tag", "mLocationClient.start()");
                            Log.e("tag", "mLocationClient=="+mLocationClient);

                            sendPost();
                            Thread.sleep(1000*3);//3秒后再次执行
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } while (isStop);

                }
            };
        }

        if (mTimer != null && mTimerTask != null) {
            Log.e("tag", "mTimer.schedule(mTimerTask, delay)");
            mTimer.schedule(mTimerTask, 0);//执行定时器中的任务
        }
    }
    /**
     * 停止定时器，初始化定时器开关
     */
    private void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        isStop = false;//重新打开定时器开关
        Log.e("tag", "isStop="+isStop);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 定位客户端参数设定，更多参数设置，查看百度官方文档
     * @return
     */
    private LocationClientOption setLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setScanSpan(1000);//每隔1秒发起一次定位
        option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系
        option.setOpenGps(true);//是否打开gps
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到该描述，不设置则在4G情况下会默认定位到“天安门广场”
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要，不设置则拿不到定位点的省市区信息
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        /*可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        该参数若不设置，则在4G状态下，会出现定位失败，将直接定位到天安门广场
         */
        return option;
    }
    /**
     * 定位监听器
     * @author User
     *
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location==null) {
                return;
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();

//            存储经纬度,先转换为String类型
            String latitude = String.valueOf( lat );
            String longitude = String.valueOf( lng );
            latmap.put( 1 , latitude );
            lonmap.put( 1 , longitude );

            latlng = new LatLng(lat, lng);
            //定位点地址信息做非空判断
            if ("".equals(location.getProvince())) {
                strLocationProvince = "未知省";
            }else {
                strLocationProvince = location.getProvince();
            }
            if ("".equals(location.getCity())) {
                strLocationCity = "未知市";
            }else {
                strLocationCity = location.getCity();
            }
            if ("".equals(location.getDistrict())) {
                strLocationDistrict = "未知区";
            }else {
                strLocationDistrict = location.getDistrict();
            }
            if ("".equals(location.getStreet())) {
                strLocationStreet = "未知街道";
            }else {
                strLocationStreet = location.getStreet();
            }
            if ("".equals(location.getStreetNumber())) {
                strLocationStreetNumber = "";
            }else {
                strLocationStreetNumber =location.getStreetNumber();
            }
            if ("".equals(location.getAddrStr())) {
                strLocationAddrStr = "";
            }else {
                strLocationAddrStr =location.getAddrStr();
            }
            //定位成功后对获取的数据依据需求自定义处理，这里只做log显示
            Log.e("tag", "latlng.lat="+lat);
            Log.e("tag", "latlng.lng="+lng);
            Log.e("tag", "strLocationProvince="+strLocationProvince);
            Log.e("tag", "strLocationCity="+strLocationCity);
            Log.e("tag", "strLocationDistrict="+strLocationDistrict);

            // 到此定位成功，没有必要反复定位
            // 应该停止客户端再发送定位请求
            if (mLocationClient.isStarted()) {
                Log.e("tag", "mLocationClient.isStarted()==>mLocationClient.stop()");
                mLocationClient.stop();

            }

        }

    }

}

