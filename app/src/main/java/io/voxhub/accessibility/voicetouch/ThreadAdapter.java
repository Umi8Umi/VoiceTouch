package io.voxhub.accessibility.voicetouch;

import android.os.Handler;
import jp.naist.ahclab.speechkit.Recognizer;
import jp.naist.ahclab.speechkit.SpeechKit;

class ThreadAdapter implements Recognizer.Listener {
    Recognizer.Listener realCode;  // accessed from main UI thread only
    private Handler handler;

    public ThreadAdapter(Recognizer.Listener realCode) {
        this.realCode = realCode;
        handler = new Handler();  // created by main thread
    }

    public void stop() {
        this.realCode = null;  // no need for synchronized
    }

    @Override
    public void onNoConnection(final String reason) {
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onNoConnection(reason);
            }
        });}

    @Override
    public void onReady(final String reason) {
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onReady(reason);
            }
        });}

    @Override
    public void onRecordingBegin(){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onRecordingBegin();
            }
        });}

    @Override       
    public void onRecordingDone(){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onRecordingDone();
            }
        });}

    @Override       
    public void onError(final Exception error){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onError(error);
            }
        });}

    @Override       
    public void onPartialResult(final String result){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onPartialResult(result);
            }
        });}

    @Override       
    public void onFinalResult(final String result){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onFinalResult(result);
            }
        });}

    @Override       
    public void onFinish(final String reason){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onFinish(reason);
            }
        });}

    @Override       
    public void onNotReady(final String reason){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onNotReady(reason);
            }
        });}

    @Override       
    public void onUpdateStatus(final SpeechKit.Status status){
        handler.post(new Runnable() {
            public void run() {
                if(realCode != null) realCode.onUpdateStatus(status);
            }
        });}
}
