package peep.example.com.peepingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import service.LocationServices;
import service.MyService;

public class MainActivity extends AppCompatActivity {
    private Button startService;
    private Button stopService;
    private Button btn_photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        startService = (Button) findViewById(R.id.start_service);
        stopService = (Button) findViewById(R.id.stop_service);
        btn_photo = (Button)findViewById( R.id.btn_photo );

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, LocationServices.class);
                startService(startIntent);
            }
        });
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopIntent = new Intent(MainActivity.this, LocationServices.class);
                stopService(stopIntent);
            }
        });
        btn_photo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( MainActivity.this , Main.class );
                startActivity( intent );
            }
        } );

        Intent intent = new Intent( MainActivity.this , MyService.class );
//        启动服务
        startService( intent );
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("tag", "MainActivity.onDestroy()");
        Intent stopIntent = new Intent(MainActivity.this, LocationServices.class);
        stopService(stopIntent);
        finish();
        super.onDestroy();
    }

}
