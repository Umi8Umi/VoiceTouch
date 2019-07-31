#!/bin/bash

./gradlew assembleRelease || exit
cp ./app/build/outputs/apk/app-release-unsigned.apk ~/apk/voicetouch.apk

rm ~/apk/voicetouch-aligned.apk
/home/serena/Downloads/build-tools/25.0.2/zipalign 4 ~/apk/voicetouch.apk ~/apk/voicetouch-aligned.apk

/home/serena/Downloads/build-tools/25.0.2/apksigner sign --ks ~/Downloads/KEYS/release.keystore ~/apk/voicetouch-aligned.apk || exit

adb uninstall io.voxhub.accessibility.voicetouch 2>&1 >/dev/null
sleep 1
adb install ~/apk/voicetouch-aligned.apk
sleep 1
adb shell am start -n io.voxhub.accessibility.voicetouch/io.voxhub.accessibility.voicetouch.SimpleActivity
