package com.stratoscientific.stethioexample;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.stratoscientific.stethio.spectrum.SpectrumGLSurfaceView;
import com.stratoscientific.stethioexample.R;

public class SecondActivity extends MainActivity {
    private SpectrumGLSurfaceView spectrumGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       LinearLayout containerLinear = findViewById(R.id.linearLayout);
        containerLinear.setBackgroundColor(Color.RED);

//        setContentView(R.layout.activity_second);
//        spectrumGLSurfaceView = findViewById(R.id.glSurfaceView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        spectrumGLSurfaceView.onPause();
    }
}
