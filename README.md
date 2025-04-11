# afsdk

Project is a flutter plugin for ANFU AF930 Android POS device,

Functions available : 

- Buzzer
- Printing
- Card Read
- Code Scanning


## Android setup

In your Flutter project's `android/build.gradle`, add Huawei's Maven repository:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://developer.huawei.com/repo/' } // Required for HMS Scan Kit
    }
}