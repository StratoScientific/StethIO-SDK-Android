package com.stratoscientific.stethioexample;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.format.DateUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.stratoscientific.stethio.StethIOExamOption;
import com.stratoscientific.stethio.enums.AudioSampleType;
import com.stratoscientific.stethio.spectrum.SpectrumGLSurfaceView;
import com.stratoscientific.stethio.StethIOBase;
import com.stratoscientific.stethio.enums.ExamType;
import com.stratoscientific.stethio.exception.InvalidAPIKeyException;
import com.stratoscientific.stethio.StethIOManager;
import com.stratoscientific.stethioexample.R;

import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1451;
    private StethIOManager stethIOManager;

    private Button startButton, stopButton, cancelButton, restartButton;
    private TextView durationTextView;
    private TextView heartRateTextView;
    private SpectrumGLSurfaceView spectrumGLSurfaceView;
    private LinearLayout containerLinear;

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

    @Override
    protected void onDestroy() {
        stethIOManager.cancel();
        super.onDestroy();
    }

    private void init() throws InvalidAPIKeyException {

        containerLinear = findViewById(R.id.container_linear);
        spectrumGLSurfaceView = findViewById(R.id.glSurfaceView);

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

        initStethIO();
        updateUI(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        spectrumGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
       spectrumGLSurfaceView.onPause();
    }

    private void updateUI(Boolean enable){
        startButton.setEnabled(enable);
        stopButton.setEnabled(enable);
        cancelButton.setEnabled(enable);
        restartButton.setEnabled(enable);
    }
    long objId;
    private void initStethIO() throws  InvalidAPIKeyException {


        StethIOBase.prepare(this);
        stethIOManager = StethIOManager.createInstance(this);
        StethIOBase.getInstance().setDebug(true);
        StethIOBase.getInstance().setListener(new StethIOBase.Listener() {
            @Override
            public void onReadyToStart() {
                startButton.setEnabled(true);
                ProgressBar progress = findViewById(R.id.loading_container);
                progress.setVisibility(View.GONE);
                Log.d(TAG, "onReadyToStart");
            }
        });
        StethIOBase.getInstance().setAPiKey("fPTukPlFivKxPA52InV3YoExe0OwS9pR3b44LyRhuH8wVI1yetj91kf64Pr5gzTn");
        updateUI(false);

        stethIOManager.setListener(new StethIOManager.Listener() {
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
            public void onRenderSpectrumGLSurfaceView(long id, ExamType examType) {
                objId = id;
                spectrumGLSurfaceView.setMap(id, examType);
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
        stethIOManager.setBpmListener(value -> runOnUiThread(() -> {
            Log.d("BPM changed", String.valueOf(value));
            heartRateTextView.setText(String.valueOf(value));
        }));

    }

    public void start() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            if (stethIOManager.isRecording()){
                if (stethIOManager.isPause() ){
                    stethIOManager.resume();
                }else {
                    stethIOManager.pause();
                }
                String text = stethIOManager.isPause() ? "Resume" :"Pause";
                startButton.setText(text);
                return;
            }
            try{
                Intent intent = getIntent();
                ExamType examType = ExamType.valueOf(intent.getStringExtra("examType"));
                AudioSampleType audioSampleType = AudioSampleType.valueOf(intent.getStringExtra("audioSampleType"));
                Float heartMinimumGain = intent.getFloatExtra("heartMinimumGain", 2.0f);
                Float lungTargetLevel = intent.getFloatExtra("lungTargetLevel", 0.1f);
                Float heartTargetLevel = intent.getFloatExtra("heartTargetLevel", 2.0f);

//                StethIOExamOption option = new StethIOExamOption(examType, audioSampleType);
                StethIOExamOption option = new StethIOExamOption(examType, audioSampleType, heartMinimumGain, lungTargetLevel, heartTargetLevel);
                stethIOManager.start(option);
                updateUI(true);
            }catch (Exception e){
                e.printStackTrace();
                showError(e.getLocalizedMessage());
            }
        }
    }

    public void stop() {
        stethIOManager.finish();
    }
    public void cancel() {
        stethIOManager.cancel();
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

    private void showError(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder.create();
        alert11.show();
    }
}