package com.example.tasktrack.models;



import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.tasktrack.R;

import java.util.concurrent.TimeUnit;


/**
 * Created by Mathias Nigsch on 22/02/2016.
 */
public class PomodoroTimer {
    private Chronometer chronometer;

    public interface CountDownTimerEvent {
        void onTick(long millisUntilFinished);

        void onFinish();
    }

    public interface OverflowStopWatchEvent {
        void onTick(long baseTime, long difference);
    }

    private static final long TIMER_INTERVAL = 1000;
    private static final long BLINK_DURATION = 600;
    private ProgressBar progressBar;

    private int workDuration;
    private int breakDuration;
    private int longBreakDuration;
    private int longBreakInterval;
    private boolean longBreakEnabled;
    private CountDownTimer countDownWorkTimer;
    private CountDownTimer countDownBreakTimer;
    private long lastTimerValue;
    private int amountWorkPeriods = 0;

    private long currentWorkTime = 0;
    private long currentBreakTime = 0;

    private TimerStatus status = TimerStatus.WAIT_FOR_WORK;
    private ObjectAnimator progressBarAnimation;
    private TextView tvTime;
    private TextView tvTimeSubtitle;

    private CountDownTimerEvent workTimerEvents;
    private CountDownTimerEvent breakTimerEvents;
    private OverflowStopWatchEvent overflowStopWatchEvent;

    private long lastChronometerValue = 0;

    public PomodoroTimer(Chronometer workTimerChronometer, ProgressBar progressBar, TextView timeText, TextView tvTimeSubtitle) {
        this.progressBar = progressBar;
        tvTime = timeText;
        this.tvTimeSubtitle = tvTimeSubtitle;
        chronometer = workTimerChronometer;
        init();
    }

    public PomodoroTimer(Chronometer dummy, ProgressBar progressBar, TextView tvTime) {
        this(dummy, progressBar, tvTime, null);
    }

    private void init() {
        countDownBreakTimer = getCountDownBreakTimer(1000 * 60 * breakDuration);
        countDownWorkTimer = getCountdownWorkTimer((long) 1000 * 60 * workDuration);
        progressBarAnimation = ObjectAnimator.ofInt(progressBar, "progress", 500, 0);

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long newValue = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
                long difference = newValue - lastChronometerValue;
                currentWorkTime += difference;
                lastChronometerValue = newValue;

                if (overflowStopWatchEvent != null) {
                    overflowStopWatchEvent.onTick(chronometer.getBase(), difference);
                }
            }
        });
    }

    private CountDownTimer getCountDownBreakTimer(long duration) {
        return new CountDownTimer(duration, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentBreakTime = ((breakDuration * 1000) - millisUntilFinished) / 1000;
                tvTime.setText(getTimeString(millisUntilFinished));

                if (breakTimerEvents != null) {
                    breakTimerEvents.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                tvTime.setText(R.string.timer_finished_text);
                if (tvTimeSubtitle != null) {
                    tvTimeSubtitle.setText("Break up");
                }
                tvTime.setAnimation(getBlinkAnimation());
                status = TimerStatus.WAIT_FOR_WORK;

                if (breakTimerEvents != null) {
                    breakTimerEvents.onFinish();
                }
                // TODO: check
                // currentBreakTime = 0;
            }
        };
    }

    private CountDownTimer getCountdownWorkTimer(long duration) {
        return new CountDownTimer(duration, TIMER_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                currentWorkTime = ((currentWorkTime * 1000) - millisUntilFinished) / 1000;
                lastTimerValue = millisUntilFinished;

                tvTime.setText(getTimeString(millisUntilFinished));

                if (workTimerEvents != null) {
                    workTimerEvents.onTick(millisUntilFinished);
                }

                //LayerDrawable ld = (LayerDrawable) progressBar.getProgressDrawable();
                // GradientDrawable sd = (GradientDrawable) ld.getDrawable(1);sd.setColor(Color.argb(100, 40, 100, 20));
                //sd.setColor((int) millisUntilFinished);
                //sd.getPaint().setColor((int) millisUntilFinished);
                //  Shader shader = new LinearGradient(0, 0, 0, progressBar.getHeight(), Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
                // sd.getPaint().s
            }

            @Override
            public void onFinish() {
                amountWorkPeriods++;
                tvTime.setText(R.string.timer_finished_text);

                if (tvTimeSubtitle != null) {
                    tvTimeSubtitle.setText("Work time up");
                }

                tvTime.setAnimation(getBlinkAnimation());
                status = TimerStatus.WAIT_FOR_BREAK;

                if (workTimerEvents != null) {
                    workTimerEvents.onFinish();
                }

                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();

                // currentWorkTime = 0;
            }
        };
    }


    public void startWork() {
        status = TimerStatus.WORK;

        currentWorkTime = 0;
        currentBreakTime = 0;

        progressBarAnimation.setDuration(1000 * 60 * workDuration); //in milliseconds
        progressBarAnimation.setInterpolator(new LinearInterpolator());
        progressBarAnimation.start();

        // Set new countdowntimer
        countDownWorkTimer = getCountdownWorkTimer(1000 * 60 * workDuration);
        countDownWorkTimer.start();
    }

    public void startBreak() {
        status = TimerStatus.BREAK;
        chronometer.stop();

        currentWorkTime = 0;
        currentBreakTime = 0;

        if (isLongBreakEnabled() && isLongBreak()) {
            // Long break -> change sub title
            progressBarAnimation.setDuration(1000 * 60 * longBreakDuration); //in milliseconds
            countDownBreakTimer = getCountDownBreakTimer(1000 * 60 * longBreakDuration);
        } else {
            progressBarAnimation.setDuration(1000 * 60 * breakDuration); //in milliseconds
            countDownBreakTimer = getCountDownBreakTimer(1000 * 60 * breakDuration);
        }

        progressBarAnimation.setInterpolator(new LinearInterpolator());
        progressBarAnimation.start();

        countDownBreakTimer.start();
    }

    public void skipBreak() {
        status = TimerStatus.WORK;
        // Skip break
        countDownBreakTimer.cancel();
        progressBarAnimation.end();
        startWork();
    }

    public void pauseWork() {
        status = TimerStatus.PAUSED_WORK;
        countDownWorkTimer.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            progressBarAnimation.pause();
        } else {
            progressBarAnimation.cancel();
        }
        tvTime.setAnimation(getBlinkAnimation());
    }

    public void resumeWork() {
        status = TimerStatus.WORK;
        countDownWorkTimer = getCountdownWorkTimer(lastTimerValue);
        countDownWorkTimer.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            progressBarAnimation.resume();
        } else {
            progressBarAnimation.start();
        }
    }

    public void stop() {
        status = TimerStatus.WAIT_FOR_WORK;
        chronometer.stop();
        countDownWorkTimer.cancel();
        countDownBreakTimer.cancel();
        progressBarAnimation.end();
        tvTime.setAnimation(null);
        tvTime.setText(R.string.timer_finished_text);
        if (tvTimeSubtitle != null) {
            tvTimeSubtitle.setText(R.string.activity_timer_subtitle_work);
        }
    }

    // http://stackoverflow.com/questions/23426201/flashing-textview-background-in-android-for-1-second-only-once
    public static Animation getBlinkAnimation() {
        Animation animation = new AlphaAnimation(1, 0);         // Change alpha from fully visible to invisible
        animation.setDuration(BLINK_DURATION);                             // duration - half a second
        animation.setInterpolator(new LinearInterpolator());    // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE);                            // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);             // Reverse animation at the end so the button will fade back in
        return animation;
    }

    public static String getTimeString(long millis) {

        String hms;
        if (TimeUnit.MILLISECONDS.toHours(millis) >= 1) {
            hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        } else {
            hms = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        }

        return hms;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setStatus(TimerStatus status) {
        this.status = status;
    }

    public TextView getTvTime() {
        return tvTime;
    }

    public void setTvTime(TextView tvTime) {
        this.tvTime = tvTime;
    }

    public TextView getTvTimeSubtitle() {
        return tvTimeSubtitle;
    }

    public void setTvTimeSubtitle(TextView tvTimeSubtitle) {
        this.tvTimeSubtitle = tvTimeSubtitle;
    }

    public CountDownTimerEvent getWorkTimerEvents() {
        return workTimerEvents;
    }

    public void setWorkTimerEvents(CountDownTimerEvent workTimerEvents) {
        this.workTimerEvents = workTimerEvents;
    }

    public CountDownTimerEvent getBreakTimerEvents() {
        return breakTimerEvents;
    }

    public void setBreakTimerEvents(CountDownTimerEvent breakTimerEvents) {
        this.breakTimerEvents = breakTimerEvents;
    }

    public int getWorkDuration() {
        return workDuration;
    }

    public void setWorkDuration(int workDuration) {
        this.workDuration = workDuration;
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(int breakDuration) {
        this.breakDuration = breakDuration;
    }

    public int getLongBreakDuration() {
        return longBreakDuration;
    }

    public void setLongBreakDuration(int longBreakDuration) {
        this.longBreakDuration = longBreakDuration;
    }

    public int getLongBreakInterval() {
        return longBreakInterval;
    }

    public void setLongBreakInterval(int longBreakInterval) {
        this.longBreakInterval = longBreakInterval;
    }

    public boolean isLongBreakEnabled() {
        return longBreakEnabled;
    }

    public void setLongBreakEnabled(boolean longBreakEnabled) {
        this.longBreakEnabled = longBreakEnabled;
    }

    public TimerStatus getStatus() {
        return status;
    }

    public int getAmountWorkPeriods() {
        return amountWorkPeriods;
    }

    public void setAmountWorkPeriods(int amountWorkPeriods) {
        this.amountWorkPeriods = amountWorkPeriods;
    }

    public boolean isLongBreak() {
        return (amountWorkPeriods > 0 && (amountWorkPeriods % longBreakInterval) == 0);
    }

    public long getCurrentWorkTime() {
        return currentWorkTime;
    }

    public void setCurrentWorkTime(long currentWorkTime) {
        this.currentWorkTime = currentWorkTime;
    }

    public long getCurrentBreakTime() {
        return currentBreakTime;
    }

    public void setCurrentBreakTime(long currentBreakTime) {
        this.currentBreakTime = currentBreakTime;
    }

    public OverflowStopWatchEvent getOverflowStopWatchEvent() {
        return overflowStopWatchEvent;
    }

    public void setOverflowStopWatchEvent(OverflowStopWatchEvent overflowStopWatchEvent) {
        this.overflowStopWatchEvent = overflowStopWatchEvent;
    }
}
