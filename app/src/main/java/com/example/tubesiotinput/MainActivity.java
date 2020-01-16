package com.example.tubesiotinput;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {
    private String suara;

    private float acelVal;
    private float acelLast;
    private float shake;

    private DatabaseReference mDatabaseReference;
    private SensorManager sensorManager;
    private Sensor mySensor;

    private Vibrator getar;

    private int active = 0;

    protected static  final int RESULT_SPEECH =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorListener, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        getar = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:{
                if(resultCode == RESULT_OK && null !=data){
                    final ArrayList<String> MasukkanSuaraAnda = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    suara = MasukkanSuaraAnda.get(0);
                    mDatabaseReference.child("Voice").setValue(suara);
                    active=0;
                }

                if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
                    showToastMessage("Audio Bermasalah");
                }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
                    showToastMessage("Client Bermasalah");
                }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
                    showToastMessage("Jaringan Bermasalah");
                }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
                    showToastMessage("Perangkat Tidak Cocok");
                }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
                    showToastMessage("Server Bermasalah");
                }
                break;

            }
        }
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if(shake < -9 && active == 0){
                myExecute();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private void myExecute(){
        showToastMessage("Masukkan Suara Anda");
        String ID_ModelBahasaIndonesia = "id";
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ID_ModelBahasaIndonesia);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, ID_ModelBahasaIndonesia);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, ID_ModelBahasaIndonesia);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, ID_ModelBahasaIndonesia);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Masukkan Suara");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        getar.vibrate(200);
        active = 1;

        try{
            startActivityForResult(intent, RESULT_SPEECH);
        }
        catch (ActivityNotFoundException a){}
    }

    private void showToastMessage(String message) {
        Toast.makeText(this, message, LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        active=0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active=0;
    }
}
