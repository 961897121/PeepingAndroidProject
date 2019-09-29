package peep.example.com.peepingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import photo.Config;
import photo.PhotoActivity;

/**
 * Created by krito on 2019/7/22.
 */

public class Main extends AppCompatActivity{
    private ImageView mIVData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        mIVData = (ImageView)findViewById( R.id.iv_data );
        startActivityForResult( new Intent( this , PhotoActivity.class ) , 0 );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        mIVData.setImageBitmap( Config.Image );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
