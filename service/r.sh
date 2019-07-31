#!/bin/bash

./gradlew assembleDebug
adb uninstall io.voxhub.accessibility.voicetouch
sleep 1
adb install ./app/build/outputs/apk/app-debug.apk
sleep 1
adb shell am start -n io.voxhub.accessibility.voicetouch/io.voxhub.accessibility.app.SimpleActivity
