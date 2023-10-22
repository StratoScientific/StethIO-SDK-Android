package com.stratoscientific.stethioexample;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.stratoscientific.stethio.StethIOBase;
import com.stratoscientific.stethio.enums.AudioSampleType;
import com.stratoscientific.stethio.enums.ExamType;
import com.stratoscientific.stethioexample.R;

public class PrepareExamActivity extends AppCompatActivity {
    private Slider heartMinimumGainSlider;
    private Slider lungTargetLevelSlider;
    private Slider heartTargetLevelSlider;
    private Spinner sampleTypeSpinner;
    private Spinner modeSpinner;
    ExamType examType = ExamType.HEART;
    AudioSampleType audioSampleType = AudioSampleType.NONE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_exam);
       init();

    }
    private void init() {
        StethIOBase.prepare(this);
        heartMinimumGainSlider = findViewById(R.id.heartMinimumGainSlider);
        lungTargetLevelSlider = findViewById(R.id.lungTargetLevelSlider);
        heartTargetLevelSlider = findViewById(R.id.heartTargetLevelSlider);

        modeSpinner = findViewById(R.id.modeSpinner);
        sampleTypeSpinner = findViewById(R.id.sampleTypeSpinner);

        setAdapter();
        StethIOBase.getInstance().setDebug(true);
        Button mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("examType", examType.name());
                i.putExtra("audioSampleType", audioSampleType.name());

                i.putExtra("heartMinimumGain", heartMinimumGainSlider.getValue());
                i.putExtra("lungTargetLevel", lungTargetLevelSlider.getValue());
                i.putExtra("heartTargetLevel", heartTargetLevelSlider.getValue());
                startActivity(i);
            }
        });
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
                try {
                    examType = modes[i];
                } catch (Exception e) {
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
                audioSampleType = audioSampleTypes[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
