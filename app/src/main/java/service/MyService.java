package service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by krito on 2019/7/27.
 */

public class MyService extends Service{
//    用于定时器停止
    private boolean isStop = false;
    private Timer timer = null;
    private TimerTask timerTask = null;
//    private String path = "http://192.168.43.6:8080/location/GetPermission";
    //测试用
    private String path = "http://192.168.43.211:8080/peeping/select";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e( "调用" , "服务onCreate()" );
    }

    //    在service中重写下面的方法，这个方法有三个返回值， START_STICKY是service被kill掉后自动重写创建
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

//        启动定时器
        if (!isStop){
            Log.e( "MyService" , "启动定时器" );
            myTimer();
        }

        Log.e("该应用被kill","现在重启service");
        return START_STICKY;
    }

    //启动定时器（5s一次），启动新的线程发post请求
    public void myTimer(){
        isStop = true;//isStop是true一会onDestroy要销毁

        if (timer == null){
            timer = new Timer( );
        }

        if (timerTask == null){
            timerTask = new TimerTask() {
                @Override
                public void run() {
                   do {
                       try {
//                        发送Post请求
                           sendPost();
                           Thread.sleep( 5000 );//5s后一次
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }while (isStop);
                }
            };
        }

        //执行定时器中的任务
        if (timerTask != null && timer != null){
            Log.e("MyService", "mTimer.schedule(mTimerTask, delay)");
            timer.schedule( timerTask , 0 );
        }
    }

//    停止定时器
    private void stopTimer(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
        if (timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }

        isStop = false;//重新打开定时器开关
        Log.e("MyService", "isStop="+isStop);
    }


//    发送Post请求
    public void sendPost(){
        new Thread( new Runnable() {
            @Override
            public void run() {
               try {
                   String hello = "hello=123";
                   byte[] bytes = hello.getBytes();

                   URL url = new URL( path );
                   HttpURLConnection con = (HttpURLConnection) url.openConnection();
                   //设置以Post方式提交数据
                   con.setRequestMethod( "POST" );
                   //设置连接超时时间
                   con.setConnectTimeout( 3000 );
                   // 请求头, 必须设置
                   con.setRequestProperty( "Content-Type" , "application/x-www-form-urlencoded" );
                   // 注意是字节长度, 不是字符长度
                   con.setRequestProperty("Content-Length", bytes + "");
                   //使用Post方式不能使用缓存
                   con.setUseCaches(false);

                   //打开输出流
                   OutputStream out = con.getOutputStream();
                   out.write( bytes );
                   out.close();

                   BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( con.getInputStream() , "UTF-8" ) );
                   String s = bufferedReader.readLine();
                   Log.e( "MyService" , "s: " + s );

                   JSONObject root = new JSONObject( s );
                   Log.e( "MyService" , "root: " +root );

                   /**
                    * 获取摄像头状态（front/back/close）
                    * 分别对应前置/后置/关闭摄像头
                    */

                   /**
                    * 获取定位状态（on/close）打开/关闭
                    */
                   String cam = root.getString( "cam" );
                   String loc = root.getString( "loc" );


                   Intent intent = new Intent(  );
                   intent.putExtra( "cam" , cam );
                   intent.putExtra( "loc" , loc );
                   intent.setAction( "Status" );
                   sendBroadcast( intent );

                   //获取状态码
                   int code = con.getResponseCode();
                   if (code == 200){
                       Log.e( "MyService" , "post请求成功" );
                   }else {
                       Log.e( "MyService" , "post请求失败状态吗：" + code );
                   }

                } catch (MalformedURLException e) {
                   e.printStackTrace();
               } catch (IOException e) {
                   e.printStackTrace();
               } catch (JSONException e) {
                   e.printStackTrace();
               }
            }
        } ).start();
    }

//    在应用销毁的时候启动服务
    @Override
    public void onDestroy() {
//        停止定时器
        if (isStop){
            Log.e("MyService", "定时器服务停止");
            stopTimer();
        }

        //销毁时重新启动Service
        Intent localIntent = new Intent();
        localIntent.setClass(this, MyService.class);
        this.startService(localIntent);
    }
}
