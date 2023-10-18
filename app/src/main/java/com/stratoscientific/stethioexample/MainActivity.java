package com.stratoscientific.stethioexample;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

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

import com.stratoscientific.stethio.enums.AudioSampleType;
import com.stratoscientific.stethio.spectrum.SpectrumGLSurfaceView;
import com.stratoscientific.stethio.StethIOBase;
import com.stratoscientific.stethio.enums.ExamType;
import com.stratoscientific.stethio.exception.InvalidAPIKeyException;
import com.stratoscientific.stethio.StethIOManager;
import com.stratoscientific.stethioexample.R;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1451;
    private StethIOManager stethIOManager;

    private Button startButton, stopButton, cancelButton, restartButton;
    private TextView durationTextView;
    private TextView heartRateTextView;
    private Spinner sampleTypeSpinner;
    private SpectrumGLSurfaceView spectrumGLSurfaceView;
    private Spinner modeSpinner;
    private LinearLayout containerLinear;
    private Switch whiteSwitch;
    private Switch debugModeSwitch;
    ExamType examType = ExamType.HEART;
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

        containerLinear = findViewById(R.id.container_linear);
        spectrumGLSurfaceView = findViewById(R.id.glSurfaceView);

        durationTextView = findViewById(R.id.duration_text_view);
        heartRateTextView = findViewById(R.id.heart_rate_text_view);
        whiteSwitch = findViewById(R.id.background_mode_switch);
        debugModeSwitch = findViewById(R.id.debug_mode_switch);

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(v -> start());

        stopButton = findViewById(R.id.stop);
        cancelButton = findViewById(R.id.cancel);
        restartButton = findViewById(R.id.restart);
        Button fullScreenButton = findViewById(R.id.full_screen_button);

        stopButton.setOnClickListener(v -> stop());
        cancelButton.setOnClickListener(v -> cancel());

        restartButton.setOnClickListener(v -> stop());
        restartButton.setOnClickListener(v -> reset());
        fullScreenButton.setOnClickListener(v -> {

            spectrumGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
            spectrumGLSurfaceView.onPause();
            LinearLayout.LayoutParams param  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            containerLinear.removeView(spectrumGLSurfaceView);
            spectrumGLSurfaceView.setLayoutParams(param);
            containerLinear.addView(spectrumGLSurfaceView);
            spectrumGLSurfaceView.bringToFront();
            startActivity(new Intent(this, SecondActivity.class));
        });

        modeSpinner = findViewById(R.id.modeSpinner);
        sampleTypeSpinner = findViewById(R.id.sampleTypeSpinner);

        setAdapter();

        initStethIO();
        updateUI(false);
        debugModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                StethIOBase.getInstance().setDebug(isChecked);
            }
        });
        whiteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
//              spectrumGLSurfaceView.setBackground(isChecked ? SpectrumGLSurfaceView.Background.WHITE : SpectrumGLSurfaceView.Background.BLACK);
            }
        });
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
        if (stethIOManager!=null){
            spectrumGLSurfaceView.setMap(objId, examType);
            return;
        }
        StethIOBase.prepare(this);
        stethIOManager = StethIOManager.createInstance(this);
        StethIOBase.getInstance().setDebug(true);
        StethIOBase.getInstance().setListener(new StethIOBase.Listener() {
            @Override
            public void onReadyToStart() {
                startButton.setEnabled(true);
                LinearLayout progress = findViewById(R.id.loading_container);
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
                    examType = modes[i];
                } catch (Exception e) {
                    Log.d(TAG, "modeSpinner:: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        AudioSampleType[] audioSampleTypes = {AudioSampleType.NONE, AudioSampleType.RAW_AUDIO, AudioSampleType.PROCESSED_AUDIO, AudioSampleType.AUTO_GAIN};
        ArrayAdapter<AudioSampleType> sampleTypesAdapter = new ArrayAdapter<AudioSampleType>(
                this,
                android.R.layout.simple_spinner_item,
                audioSampleTypes);
        sampleTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleTypeSpinner.setAdapter(sampleTypesAdapter);
        sampleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "sampleTypeSpinner:onItemClick: " + i + " - " + l);
                stethIOManager.setAudioSampleType(audioSampleTypes[i]);
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
                stethIOManager.start(examType);
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