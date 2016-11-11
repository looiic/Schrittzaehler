package com.example.loicjermann.schrittzaehler;

//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;

//public class MainActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}


//package ch.appquest.schrittzaehler;

        import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private final int INTENT_REQUEST_QR_CODE_START = 0;
    private final int INTENT_REQUEST_WALK = 2;

    private ArrayList<String> list;
    private Button btnQr;
    private Button btnStart;
    private Button btnEnd;
    private TextView lblQr;
    private String qrContent;
    private String start;
    private String end;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = new ArrayList<String>();
        btnQr = (Button)findViewById(R.id.btnQr);
        btnStart =(Button)findViewById(R.id.btnStart);
        lblQr = (TextView)findViewById(R.id.lblQr);
        btnEnd = (Button)findViewById(R.id.btnEnd);
        initListener();
    }

    private void initListener() {
        btnQr.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, INTENT_REQUEST_QR_CODE_START);
            }
        });

        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TurnActivity.class);
                intent.putExtra("content", qrContent);
                startActivityForResult(intent, INTENT_REQUEST_QR_CODE_START);
            }
        });

        btnEnd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, INTENT_REQUEST_WALK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_REQUEST_QR_CODE_START:
                if (resultCode == RESULT_OK) {
                    qrContent = data.getStringExtra("SCAN_RESULT");
                    lblQr.setText(qrContent);

                    try {
                        JSONObject js = new JSONObject(qrContent);
                        start = js.getString("startStation");

                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }

                break;
            case INTENT_REQUEST_WALK:
                if (resultCode == RESULT_OK) {
                    qrContent = data.getStringExtra("SCAN_RESULT");

                    try {
                        list = new ArrayList<String>();
                        JSONObject js = new JSONObject(qrContent);
                        end = js.getString("endStation");

                        Toast.makeText(getApplicationContext(), "{\"startStation\": " + start + ", \"endStation\": "+ end + "}", Toast.LENGTH_SHORT).show();
                        log();
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }

                break;
        }
    }
    private void log() {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra("ch.appquest.taskname", "Schrittz�hler");
        // Achtung, je nach App wird etwas anderes eingetragen (siehe Tabelle ganz unten):
        intent.putExtra("ch.appquest.logmessage", "{\"startStation\": " + start + ", \"endStation\": "+ end + "}");

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
