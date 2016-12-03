package sharescreen.wanjian.com.server;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import sharescreen.wanjian.com.a.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ScreeenSend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tv = (TextView) findViewById(R.id.tv);
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecorderManager.getInstance(MainActivity.this)
                        .startRecorder(MainActivity.this, 0.5f);
            }
        });

        findViewById(R.id.jieshu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecorderManager.getInstance(MainActivity.this)
                        .stopRecorder();
            }
        });
        findViewById(R.id.act2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SecondActivity.class));
            }
        });
        new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                tv.setText(new Date().toLocaleString());

                sendEmptyMessageDelayed(0,1000);
            }
        }.sendEmptyMessage(0);


    }
}
