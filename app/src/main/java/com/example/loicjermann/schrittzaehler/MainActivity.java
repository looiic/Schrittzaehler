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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements StepListener {
    private final int INTENT_REQUEST_QR_CODE_START = 0;
    private final int INTENT_REQUEST_WALK = 2;

    private ArrayList<String> list;
    private Button btnQr;
    private Button btnStart;
    private Button btnEnd;
    private Button btnNextStep;
    private TextView lblQr, lblQr2, lblStepCounter, lblSteps;
    private String qrContent, start, end, walk;
    private Boolean esHatDaten = false;

    TextToSpeech ttobj;


    private SensorManager sensorManager;
    private Sensor sensor;
    private StepCounter stepCounter;

    private int steps = 0, index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepCounter = new StepCounter(this);


        list = new ArrayList<String>();
        btnQr = (Button)findViewById(R.id.btnQr);
        btnStart =(Button)findViewById(R.id.btnStart);
        lblQr = (TextView)findViewById(R.id.lblQr);
        lblQr2 = (TextView) findViewById(R.id.lblQr2);
        lblStepCounter = (TextView) findViewById(R.id.lblStepCounter);
        lblSteps = (TextView) findViewById(R.id.lblSteps);
        lblSteps.setText(String.valueOf(steps));
        btnEnd = (Button)findViewById(R.id.btnEnd);
        initListener();


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                ttobj.setLanguage(Locale.GERMAN);
            }
        }
        );
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

                    try {

                        JSONObject jsonObj = new JSONObject(qrContent);
                        JSONArray input = jsonObj.getJSONArray("input");

                        start = jsonObj.getString("startStation");

                        ArrayList<String> list = new ArrayList<String>();
                        JSONArray jsonArray = (JSONArray)input;
                        if (jsonArray != null) {
                            int len = jsonArray.length();
                            for (int i = 0; i < len; i++) {
                                list.add(jsonArray.get(i).toString());
                            }
                            esHatDaten = true;
                        }


                    lblQr.setText(list.get(index));
                    lblQr2.setText(list.get(index + 1));

                    } catch (Exception e) {
                        e.getStackTrace();
                        lblQr.setText("Failed");
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
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

                        Toast.makeText(getApplicationContext(), "{\"task\": \"Schrittzaehler\", \"startStation\": " + start + ", \"endStation\": "+ end + "}", Toast.LENGTH_SHORT).show();
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
        intent.putExtra("ch.appquest.logmessage", "{\"task\": \"Schrittzaehler\", \"startStation\": " + start + ", \"endStation\": "+ end + "}");

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounter != null) {
            sensorManager.registerListener(stepCounter, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepCounter != null) {
            sensorManager.unregisterListener(stepCounter);
        }

    }

    @Override
    public void onStep() {
        this.steps++;
        lblSteps.setText(String.valueOf(steps));
        if(esHatDaten == true){
            try{
                String eintrag1 = this.list.get(index);
                //if (steps == 1){
                if (eintrag1.equals("10")) {
                    index++;
                    ttobj.speak("links", TextToSpeech.QUEUE_FLUSH, null);
                    steps = 0;
                    lblSteps.setText(String.valueOf(steps));
                    //lblQr.setText(list.get(index + 1));
                    //lblQr2.setText(list.get(index + 2));
                }
            }catch(Exception e){
                Toast.makeText(getApplicationContext(),
                        e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
