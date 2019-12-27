#!/bin/bash

./gradlew assembleDebug
# adb uninstall io.voxhub.accessibility.servicecode
# sleep 1

#adb install ./service/build/outputs/aar/service-debug.aar
#sleep 1

adb uninstall io.voxhub.accessibility.voicetouch
sleep 1
adb install ./app/build/outputs/apk/debug/app-debug.apk
sleep 1

adb shell am start -n io.voxhub.accessibility.voicetouch/io.voxhub.accessibility.voicetouch.SimpleActivity

# adb shell am start -n io.voxhub.accessibility.servicecode/io.voxhub.accessibility.servicecode.MyAccessibilityService
