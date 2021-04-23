package com.stratoscientific.stethioexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.stratoscientific.steth_io_sdk.StethIO;

public class MainActivity extends AppCompatActivity {

    Button start;
    Button play;
    boolean recording=false;
    StethIORecorder stethIORecorder;
    StethIO stethIO;
    float[] buffer;
    private PermissionCallback permissionCallBack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start=findViewById(R.id.start);
        play=findViewById(R.id.play);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording)
                checkAudioPermission(new PermissionGrantedCallback() {
                    @Override
                    public void onGranted() {
                        startRecording();

                    }
                });
            }

        });



        StethIO stethIO=new StethIO(this);
    }

    private void startRecording() {
        recording=true;
        try {
            stethIO=new StethIO(this);
            stethIO.setAPiKey("fPTukPlFivKxPA52InV3YoExe0OwS9pR3b44LyRhuH8wVI1yetj91kf64Pr5gzTn")
                    .prepare();
            stethIO.setExamType(StethIO.type.LUNG);
            start.setEnabled(false);
            play.setEnabled(false);
            stethIORecorder = new StethIORecorder(this);
            stethIORecorder.setStethIORecorderCallback(new StethIORecorder.StethIORecorderCallback() {
                @Override
                public void onSamplesGenerated(final float[] samples, int bufferSize, final AudioTrack track, long time) {

                    try {
                        stethIO.processStethAudio(samples, new StethIO.FilteredBuffer() {
                            @Override
                            public void getAudioBuffer(float[] floats) {
                                Log.e("Buffer", String.valueOf(floats.length));
                                track.write(floats, 0, floats.length, AudioTrack.WRITE_BLOCKING);

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRecordingComplete() {
//               buffer=stethIO.getCompleteAudioBuffer();
                    stethIO.stopFiltering();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            start.setEnabled(true);
                        }
                    });
                }

            });

            stethIORecorder.setBufferSize(300);

            stethIORecorder.setTimeToBeRecordedInMillis(30*1000);

            stethIORecorder.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public boolean checkIfPermissionIsGranted(String permission) {

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, permission);

        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String[] permissions, PermissionCallback permissionCallBack) {

        this.permissionCallBack = permissionCallBack;

        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1002);

    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(reqCode, permissions, grantResults);
        if (reqCode == 10002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionCallBack.onPermissionGranted();
            } else {
                permissionCallBack.onPermissionRejected();
            }
        }
    }

    private void checkAudioPermission(final PermissionGrantedCallback grantedCallback) {

        if (!checkIfPermissionIsGranted(android.Manifest.permission.RECORD_AUDIO)) {
            requestPermission(new String[]{Manifest.permission.RECORD_AUDIO}, new PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    checkAudioPermission(grantedCallback);
                }

                @Override
                public void onPermissionRejected() {
                    Toast.makeText(MainActivity.this,"Permission Rejected",Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            grantedCallback.onGranted();
        }
    }


}
