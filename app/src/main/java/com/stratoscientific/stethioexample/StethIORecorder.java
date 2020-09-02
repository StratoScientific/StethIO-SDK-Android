package com.stratoscientific.stethioexample;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class StethIORecorder {

    private Context context;

    //default sample-rate
    private static final int SAMPLE_RATE_IN_HZ = 44100;

    //audio record for recording input
    private AudioRecord audioRecord = null;

    //audio track for routing back
    private AudioTrack track = null;

    //audio manager
    private AudioManager audioManager = null;

    //to store the time to recording
    private long startTime;

    //time in millis that indicates for the time to be recording
    private long timeToBeRecordedInMillis;

    //based on this size, the samples will be recorded
    private int bufferSize;

    //thread to handle audio recording & play-back
    private Thread myThread;

    private StethIORecorderCallback stethIORecorderCallback;

    public StethIORecorder(Context context) {
        this.context = context;
    }

    public void setStethIORecorderCallback(StethIORecorderCallback stethIORecorderCallback) {
        this.stethIORecorderCallback = stethIORecorderCallback;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setTimeToBeRecordedInMillis(long timeToBeRecordedInMillis) {
        this.timeToBeRecordedInMillis = timeToBeRecordedInMillis;
    }

    private boolean isRunning = true;

    private boolean isCancelled = false;
    private boolean isPaused = false;
    private long pauseTime;

    /*
     *
     * Method that initialise AudioRecord & AudioTrack,
     *  sets preferences and start recording audio.*/
    private void initRecording() {

        isRunning = true;

        float[] buffer;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        setPreferences();

        audioRecord.startRecording();

        track.play();

        while (isRunning && !isCancelled ) {

            if (!isPaused){
                buffer = new float[bufferSize];

                audioRecord.read(buffer, 0, bufferSize, AudioRecord.READ_BLOCKING);

                stethIORecorderCallback.onSamplesGenerated(buffer, bufferSize, track, timeToBeRecordedInMillis - (System.currentTimeMillis() - startTime));

                isRunning = (System.currentTimeMillis() - startTime) < timeToBeRecordedInMillis;
            }

        }
        release();



            if (!isCancelled) {

                stethIORecorderCallback.onRecordingComplete();
            }
            myThread.interrupt();






    }

    /*
     *
     * Method to set the AudioRecord and AudioTrack preferences.*/
    private void setPreferences() {

        if (bufferSize <= 0)
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_FLOAT, bufferSize);

        track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_FLOAT, bufferSize, AudioTrack.MODE_STREAM);

        track.setPlaybackRate(SAMPLE_RATE_IN_HZ);

        for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)) {
            if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC) {
                audioRecord.setPreferredDevice(device);
            } else if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                track.setPreferredDevice(device);
            }
        }
    }

    /*
     *
     * Method to stop recording.*/
    public void cancelRecording() throws IllegalStateException {

        isCancelled = true;
        isRunning = false;


    }


    private void release() {

        isRunning = false;


        if (AudioRecord.STATE_INITIALIZED == audioRecord.getState()) {
            audioRecord.stop();
            audioRecord.release();
        }

        if (track != null && AudioTrack.STATE_INITIALIZED == track.getState()) {

            if (track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {

                track.stop();

            }

            track.release();

            if (audioManager != null)
                audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    /*
     *
     * Method to initialise thread and starts recording.*/
    public void startRecording() {

        startTime = System.currentTimeMillis();

        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                initRecording();
            }
        });

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        myThread.start();
    }

    public void isPausedTrue() {
        isPaused = true;
        pauseTime = System.currentTimeMillis();

    }

    public void isPausedFalse() {

        startTime = System.currentTimeMillis()-(pauseTime-startTime);
        isPaused = false;

    }

    /*
     *
     * Interface for handling actions and results.*/
    public interface StethIORecorderCallback {

        void onSamplesGenerated(float[] samples, int bufferSize, AudioTrack track, long time);

        void onRecordingComplete();

    }
}
