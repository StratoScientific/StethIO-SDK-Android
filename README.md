Steth IO SDK
=======

## Download Example
	Clone this repositary to install the example.

## Requirements
- The recording frequency must be 44.1khz because the filters are designed in such way.
- The heart/lung filters will work as expected only with Steth IO hardware.

## Installation


1. Add aar file in app/lib folder

2. Include the library in **build.grade** (Module)

     ```
     dependencies {
         implementation files('libs/steth-io-sdk.aar')
     }
     ```


### Using SDK
1. In Activity
    ```

    //Initializes class
    StethIO stethIO=new StethIO(this);
    //Set Api key and prepares for filtering
    stethIO.setAPiKey("YOUR_API_KEY")
                        .prepare();

    //Processing Audio
    stethIO.processStethAudio(samples, new StethIO.FilteredBuffer() {
        @Override
        public void getAudioBuffer(float[] floats) {
            //Perform Action
        }
    });

    //Stop Filtering
    stethIO.stopFiltering();

    ```
    2. Other Methods


	| Method  Name | Required | Default Value | Example |
	|--|--|--|--|
	| setBufferSize(int) | NO | 300 | setBufferSize(300);
	| setExamType(type) | NO | HEART | stethIO.setExamType(StethIO.type.LUNG);


## Features
- Process Audio.


