package photo;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

/**
 * Created by krito on 2019/7/22.
 */

public class Config {
    public static Bitmap Image;

    /**
     * 读取图片属性：旋转的角度
     *
     *@param path 图片绝对路径
     * @return degree旋转的角度
     */

    public static int readPictureDegree(String path){
        int degree = 0;//旋转角度
        try{
            ExifInterface exifInterface = new ExifInterface( path );
            int orientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION , ExifInterface.ORIENTATION_NORMAL );
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 270;
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
            Log.e("tag" , "path路径问题");
        }
        return degree;
    }

    /*
    * 旋转图片
    * @param angle
     * @param bitmap
     * @return Bitmap
    * */
    public static Bitmap rotaingImageView(int angle , Bitmap bitmap ){
//        旋转图片动作
        Matrix matrix = new Matrix(  );
        matrix.postRotate( angle );
//        创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap( bitmap , 0 , 0 ,
                bitmap.getWidth() , bitmap.getHeight() , matrix ,true);
        return resizedBitmap;
    }


}

