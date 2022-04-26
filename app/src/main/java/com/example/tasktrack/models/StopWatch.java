package com.example.tasktrack.models;

import android.os.SystemClock;
import android.widget.Chronometer;

/**
 * Represents a stopwatch using a chronometer.
 * Created by Mathias Nigsch on 22/02/2016.
 */
public class StopWatch {
    public interface OnBeforeStartListener {
        void onBeforeStart();
    }

    private Chronometer chronometer;
    private long lastStopTime;
    private long currentMeasuredTime;

    private OnBeforeStartListener onBeforeStartListener;
    private String format;
    private Chronometer.OnChronometerTickListener onChronometerTickListener;

    public StopWatch(Chronometer chronometer) {
        this.chronometer = chronometer;
        init();
    }

    private void init() {
        // http://stackoverflow.com/questions/4897665/android-chronometer-format
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer c) {
                currentMeasuredTime = SystemClock.elapsedRealtime() - c.getBase();
                if (currentMeasuredTime > 3600000L) {
                    c.setFormat("0%s");
                } else {
                    c.setFormat("00:%s");
                }
                onChronometerTickListener.onChronometerTick(c);
            }
        });
    }

    public void start() {
        onBeforeStartListener.onBeforeStart();

        // on first start
        if (lastStopTime == 0) {
            chronometer.setFormat("00:%s");
            chronometer.setBase(SystemClock.elapsedRealtime());
            // on resume after pause
        } else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - lastStopTime);
            chronometer.setBase(chronometer.getBase() + intervalOnPause);
        }
        chronometer.start();
    }

    public void pause() {
        chronometer.stop();
        lastStopTime = SystemClock.elapsedRealtime();
    }

    public void stop() {
        chronometer.stop();
        lastStopTime = 0;
    }

    public Chronometer getChronometer() {
        return chronometer;
    }

    public void setChronometer(Chronometer chronometer) {
        this.chronometer = chronometer;
    }

    public long getLastStopTime() {
        return lastStopTime;
    }

    public void setLastStopTime(long lastStopTime) {
        this.lastStopTime = lastStopTime;
    }

    public Chronometer.OnChronometerTickListener getOnChronometerTickListener() {
        return onChronometerTickListener;
    }

    public void setOnChronometerTickListener(Chronometer.OnChronometerTickListener onChronometerTickListener) {
        this.onChronometerTickListener = onChronometerTickListener;
    }

    public long getCurrentMeasuredTime() {
        return currentMeasuredTime;
    }

    public void setCurrentMeasuredTime(long currentMeasuredTime) {
        this.currentMeasuredTime = currentMeasuredTime;
    }

    public OnBeforeStartListener getOnBeforeStartListener() {
        return onBeforeStartListener;
    }

    public void setOnBeforeStartListener(OnBeforeStartListener onBeforeStartListener) {
        this.onBeforeStartListener = onBeforeStartListener;
    }
}
