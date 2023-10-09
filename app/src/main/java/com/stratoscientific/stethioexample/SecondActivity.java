package com.stratoscientific.stethioexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.stratoscientific.stethio.spectrum.SpectrumGLSurfaceView;
import com.stratoscientific.stethioexample.R;

public class SecondActivity extends AppCompatActivity {
    private SpectrumGLSurfaceView spectrumGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        spectrumGLSurfaceView = findViewById(R.id.glSurfaceView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        spectrumGLSurfaceView.onPause();
    }
}
