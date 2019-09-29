package peep.example.com.peepingsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import photo.BackActivity;
import photo.FrontActivity;
import service.LocationServices;
import service.MyService;

public class StartPeepActivity extends AppCompatActivity {
    private MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.start );

        Intent intent = new Intent( StartPeepActivity.this , MyService.class );
//        启动服务
        startService( intent );

        //动态注册广播
        initMessageReceiver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
        Log.d("tag", "MainActivity.onDestroy()");
        Intent stopIntent = new Intent(StartPeepActivity.this, LocationServices.class);
        stopService(stopIntent);
        unregisterReceiver( messageReceiver );
        finish();
    }
    private void initMessageReceiver()
    {
        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Status");
        registerReceiver(messageReceiver,filter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cam = intent.getStringExtra( "cam" );
            String loc = intent.getStringExtra( "loc" );
            Log.e( "StartPeepActivity" , "cam:" + cam );
            Log.e( "StartPeepActivity" , "loc:" + loc );

            Intent intent1 = new Intent( StartPeepActivity.this , LocationServices.class );
            if (loc.equals( "on" )){
                startService( intent1 );
            }else if (loc.equals( "close" )){
                stopService( intent1 );
            }

            if (cam.equals( "front" )){
                Intent go_front = new Intent( StartPeepActivity.this , FrontActivity.class);
                startActivity( go_front );
            }else if (cam.equals( "back" )){
                Intent go_back = new Intent( StartPeepActivity.this , BackActivity.class );
                startActivity( go_back );
            }
        }
    }

}
