package com.stratoscientific.stethioexample;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.stratoscientific.stethio.StethIOManagerListener;
import com.stratoscientific.stethio.enums.ExamType;
import com.stratoscientific.stethio.exception.InvalidAPIKeyException;
import com.stratoscientific.stethio.enums.SampleType;
import com.stratoscientific.stethio.StethIOManager;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1451;
    private StethIOManager stethIO;

    private Button startButton, stopButton, cancelButton, restartButton;
    private TextView durationTextView;
    private TextView heartRateTextView;
    private Spinner sampleTypeSpinner;

    private Spinner modeSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            init();
        } catch ( InvalidAPIKeyException e) {
            e.printStackTrace();
        }
    }

    private void init() throws InvalidAPIKeyException {

        durationTextView = findViewById(R.id.duration_text_view);
        heartRateTextView = findViewById(R.id.heart_rate_text_view);

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(v -> start());

        stopButton = findViewById(R.id.stop);
        cancelButton = findViewById(R.id.cancel);
        restartButton = findViewById(R.id.restart);

        stopButton.setOnClickListener(v -> stop());
        cancelButton.setOnClickListener(v -> cancel());

        restartButton.setOnClickListener(v -> stop());
        restartButton.setOnClickListener(v -> reset());

        modeSpinner = findViewById(R.id.modeSpinner);
        sampleTypeSpinner = findViewById(R.id.sampleTypeSpinner);

        setAdapter();

        initStethIO();
        updateUI(false);
    }

    private void updateUI(Boolean enable){
        startButton.setEnabled(enable);
        stopButton.setEnabled(enable);
        cancelButton.setEnabled(enable);
        restartButton.setEnabled(enable);
    }
    private void initStethIO() throws  InvalidAPIKeyException {
        StethIOManager.prepare(this);
        stethIO = StethIOManager.getInstance();
        stethIO.setDebug(true);
        stethIO.setClearWhenStop(false);
        stethIO.setListener(new StethIOManagerListener() {
            @Override
            public void onReadyToStart() {
                startButton.setEnabled(true);
                LinearLayout progress = findViewById(R.id.loading_container);
                progress.setVisibility(View.GONE);
                Log.d(TAG, "onReadyToStart");
            }

            @Override
            public void onStarted() {
                runOnUiThread(() -> {
                    updateUI(true);
                    startButton.setText("Pause");
                });

                Log.d(TAG,"onStarted");
            }

            @Override
            public void onCancelled() {
                Log.d(TAG, "onCancelled");
            }

            @Override
            public void onReceivedDuration(long milliseconds) {
//                Log.d(TAG, "onReceivedDuration" + milliseconds/1000);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        durationTextView.setText(DateUtils.formatElapsedTime(milliseconds/1000));
                    }
                });
            }

            @Override
            public void onFinished(File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(false);
                        startButton.setEnabled(true);
                        startButton.setText("Start");
                        heartRateTextView.setText("");
                    }
                });
                Log.d(TAG, "onFinished" + file);
            }
        });
        stethIO.setBpmListener(value -> runOnUiThread(() -> {
            Log.d("BPM changed", String.valueOf(value));
            heartRateTextView.setText(String.valueOf(value));
        }));
        stethIO.setAPiKey("fPTukPlFivKxPA52InV3YoExe0OwS9pR3b44LyRhuH8wVI1yetj91kf64Pr5gzTn");

    }

    private void setAdapter() {
        ExamType[] modes = {ExamType.HEART, ExamType.LUNGS, ExamType.VASCULAR};
        ArrayAdapter<ExamType> spinnerArrayAdapter = new ArrayAdapter<ExamType>(
                this,
                android.R.layout.simple_spinner_item,
                modes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(spinnerArrayAdapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "modeSpinner:onItemClick: " + i + " - " + l);
                try {
                    stethIO.setExamType(modes[i]);
                } catch (Exception e) {
                    Log.d(TAG, "modeSpinner:: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        SampleType[] sampleTypes = {SampleType.NONE, SampleType.RAW_AUDIO, SampleType.PROCESSED_AUDIO};
        ArrayAdapter<SampleType> sampleTypesAdapter = new ArrayAdapter<SampleType>(
                this,
                android.R.layout.simple_spinner_item,
                sampleTypes);
        sampleTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleTypeSpinner.setAdapter(sampleTypesAdapter);
        sampleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "sampleTypeSpinner:onItemClick: " + i + " - " + l);
                stethIO.setSampleType(sampleTypes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void start() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            updateUI(true);
            if (stethIO.isRecording()){
                if (stethIO.isPause() ){
                    stethIO.resume();
                }else {
                    stethIO.pause();
                }
                String text = stethIO.isPause() ? "Resume" :"Pause";
                startButton.setText(text);
                return;
            }
            try{
                stethIO.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        stethIO.finish();
    }
    public void cancel() {
        stethIO.cancel();
        updateUI(false);
        startButton.setEnabled(true);
        startButton.setText("Start");
        heartRateTextView.setText("");
    }

    public void reset() {
        cancel();
        start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            start();
        }
    }
}