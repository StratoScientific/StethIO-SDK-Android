Steth IO SDK
=======

## Download Example
	Clone this repositary to install the example.

## Requirements
- The recording frequency must be 44.1khz because the filters are designed in such way.
- The heart/lung filters will work as expected only with Steth IO hardware.

## Installation

1. Find the aar file in example project's app/libs and download it.

2. Add aar file in your project's app/libs folder.

3. Include the library in **build.grade** (Module)

     ```
     dependencies {
         implementation files('libs/steth-io-sdk.aar')
     }
     ```


### Using SDK
1. In Activity
    ```

   //Initializer
    StethIO stethIO=new StethIO(this);
    
   //Enter your API key here
    stethIO.setAPiKey("YOUR_API_KEY");
            
   //Pass 'GlSurfaceView' instance to graphview parameter.
   //This view will render the graph visualisation.
   //GlSurfaceView visibility should be View.GONE before passing to stethIO.setGlSurfaceView(...) method.
    stethIO.setGlSurfaceView(glSurfaceView);
            
   //Optional listener to get samples
    stethIO.setSamplesGeneratedListener(new StethIO.SamplesGeneratedListener() {
               @Override
               public void onSamplesGenerated(float[] floats) {
                          //Perform Action
               }
   
               @Override
               public void onRecordingComplete(float[] floats) {
                          //Perform Action
               }
   
               @Override
               public void onRecordingComplete(File file) {
                   runOnUiThread(() -> {
                          //Perform Action
                            Log.d("File saved to ", file.getPath());
                   });
               }
           });
   
   //Optional listener to get error messages
    stethIO.setErrorListener(errorMsg -> runOnUiThread(() -> {
                          //Perform Action
                            Log.e("Error", errorMsg);
           }));
   
   //Optional listener to get BPM string when exam type is Heart
    stethIO.setBpmListener(bpmString -> runOnUiThread(() -> {
                          //Perform Action
                            Log.d("BPM changed", bpmString);
           }));
    
   //Here we need to process the biquad files and apply filter
    stethIO.prepare();

   //Set the filter mode to StethIO.type.HEART/StethIO.type.LUNG
    stethIO.setExamType(StethIO.type.HEART);
    
   //Set the sample type to StethIO.SampleType.NONE/StethIO.SampleType.PROCESSED_AUDIO/StethIO.SampleType.RAW_AUDIO
    stethIO.setSampleType(StethIO.SampleType.PROCESSED_AUDIO);
   
   //This will start the recording
    stethIO.startRecording();
    
   //This will stop the recording
    stethIO.stopRecording();

    ```
## Important ⚠️
The API_KEY in the example application will only work for the example application. Using the same key in another application will not work.

## Author
StethIO, stethio@ionixxtech.com

## License
Steth-IO-Android is available under the MIT license. See the LICENSE file for more info.
