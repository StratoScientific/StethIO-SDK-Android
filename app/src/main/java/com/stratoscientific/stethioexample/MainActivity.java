package com.stratoscientific.stethioexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.stratoscientific.steth_io_sdk.InvalidBundleException;
import com.stratoscientific.steth_io_sdk.StethIO;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1451;

    private StethIO stethIO;

    private Button startButton, stopButton;

    private Spinner sampleTypeSpinner;

    private Spinner modeSpinner;

    private GLSurfaceView glSurfaceView = null;

    private PermissionCallback permissionCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            init();
        } catch (InvalidBundleException e) {
            e.printStackTrace();
        }
    }

    private void init() throws InvalidBundleException {

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(v -> start());

        stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(v -> stop());

        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setVisibility(View.GONE);

        modeSpinner = findViewById(R.id.modeSpinner);
        sampleTypeSpinner = findViewById(R.id.sampleTypeSpinner);

        setAdapter();

        initStethIO();
    }

    private void initStethIO() throws InvalidBundleException {

        stethIO = new StethIO(this);
        System.out.println("Unique Id "+stethIO.getDeviceIMEI(this));
        stethIO.heartMinimumGain(0.7f);
        stethIO.heartTargetLevel(0.7f);
        stethIO.lungTargetLevel(0.7f);
        stethIO.setAPiKey("###YOUR_API_KEY###");
        stethIO.setGlSurfaceView(glSurfaceView);
        stethIO.setSamplesGeneratedListener(new StethIO.SamplesGeneratedListener() {
            @Override
            public void onSamplesGenerated(float[] floats) {

            }

            @Override
            public void onRecordingComplete(float[] floats) {

            }

            @Override
            public void onRecordingComplete(File file) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "File saved to " + file.getPath(), Toast.LENGTH_LONG).show();
                    Log.d("File saved to ", file.getPath());
                });
            }
        });
        stethIO.setErrorListener(errorMsg -> runOnUiThread(() -> {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            Log.e("Error", errorMsg);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }));
        stethIO.setBpmListener(bpmString -> runOnUiThread(() -> {
            Log.d("BPM changed", bpmString);
        }));
        stethIO.prepare();

    }

    private void setAdapter() {
        StethIO.type[] modes = {StethIO.type.HEART, StethIO.type.LUNG};
        ArrayAdapter<StethIO.type> spinnerArrayAdapter = new ArrayAdapter<StethIO.type>(
                this,
                android.R.layout.simple_spinner_item,
                modes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(spinnerArrayAdapter);

        StethIO.SampleType[] sampleTypes = {StethIO.SampleType.NONE, StethIO.SampleType.RAW_AUDIO, StethIO.SampleType.PROCESSED_AUDIO};
        ArrayAdapter<StethIO.SampleType> sampleTypesAdapter = new ArrayAdapter<StethIO.SampleType>(
                this,
                android.R.layout.simple_spinner_item,
                sampleTypes);
        sampleTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleTypeSpinner.setAdapter(sampleTypesAdapter);
    }

    public void start() {
        if (!checkIfPermissionIsGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkIfPermissionIsGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkIfPermissionIsGranted(Manifest.permission.RECORD_AUDIO)) {
            requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO}, new PermissionCallback() {
                @Override
                public void onPermissionGranted() {

                }

                @Override
                public void onPermissionRejected() {

                }
            });
        } else {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            stethIO.setExamType((StethIO.type) modeSpinner.getSelectedItem());
            stethIO.setSampleType((StethIO.SampleType) sampleTypeSpinner.getSelectedItem());
            try{
                stethIO.startRecording();
            }catch (Exception e){

            }

        }
    }

    public void stop() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        stethIO.stopRecording();
    }

    public boolean checkIfPermissionIsGranted(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String[] permissions, PermissionCallback permissionCallBack) {
        this.permissionCallBack = permissionCallBack;
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
}
