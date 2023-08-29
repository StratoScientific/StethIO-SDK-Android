Steth-IO-SDK
=======

## Example
	Clone this repositary to install the example.

## Requirements
- The recording frequency must be 44.1khz because the filters are designed in such way.
- The heart/lung filters will work as expected only with Steth IO hardware.

## Installation

1. Find the **aar file** in example project's **app/libs** folder and download it.

2. Add **aar file** in your project's **app/libs** folder.

3. Include the library in **build.grade** (Module)

     ```
     dependencies {
         implementation files('libs/steth-io-sdk.aar')
     }
     ```


### Using SDK

1. Initialize setup
```
    StethIOManager.prepare(this);
    StethIOManager stethIO = StethIOManager.getInstance();
    stethIO.setDebug(true);// default false
    stethIO.setClearWhenStop(false);  default false
    stethIO.setAPiKey("fPTukPlFivKxPA52InV3YoExe0OwS9pR3b44LyRhuH8wVI1yetj91kf64Pr5gzTn");
            
```
2. Listener of recording callback
```
stethIO.setListener(new StethIOManagerListener() {
      @Override
      public void onReadyToStart() {
          Log.d(TAG, "onReadyToStart");
      }

      @Override
      public void onStarted() {
          Log.d(TAG,"onStarted");
      }

      @Override
      public void onCancelled() {
          Log.d(TAG, "onCancelled");
      }

      @Override
      public void onReceivedDuration(long milliseconds) {
          Log.d(TAG, "onReceivedDuration" + milliseconds/1000);
      }

      @Override
      public void onFinished(File file) {
          Log.d(TAG, "onFinished" + file);
            }
});

 stethIO.setBpmListener(value -> runOnUiThread(() -> {
     Log.d("BPM changed", String.valueOf(value));
}));
```
3. Actions

|Param    Type    | Required   | Description  | Exception
|:--- | --- | :---:| :--- | :---:|
|setAPiKey| Function|✅|requied valid api key| `InvalidAPIKeyException`
|isPause| Function | | recording of pause status `Boolean`|
|isRecording| Function | | recording is active or not `Boolean`|
|isHeadphonesPlugged| Function | | Headphones is Connected or not  `Boolean`|
|isBluetoothPlugged| Function | | isBluetoothDevice Plugged  or not `Boolean`|
|setEnvironment| Function | | default `PRODUCTION`, change the environment `STAGING` or `PRODUCTION`|
|setExamType| Function |✅|ExamType  `HEART`,`LUNG`|
|setSampleType| Function |✅|SampleType `NONE`, `RAW_AUDIO`, `PROCESSED_AUDIO`|
|setDebug| Function ||default value is `false`|
|start| Function |✅|start the exam, when API key are valid and audio permission|`InvalidAPIKeyException`, `AudioPermissionException`
|pause| Function | | pause  recording, if recording is running|
|resume| Function | | resume  recording, if recording is pause|
|cancel| Function | | cancel  recording, if recording is running|
|finish| Function | | finish  recording, if recording is running|

## Important ⚠️
The API_KEY in the example application will only work for the example application. Using the same key in another application will not work.

## Author
StethIO, stethio@ionixxtech.com

## License
Steth-IO-Android is available under the MIT license. See the LICENSE file for more info.
