package com.example.tasktrack.activities;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import com.example.tasktrack.R;
import com.example.tasktrack.database.DataSource;
import com.example.tasktrack.dialogs.TimerSelectionDialogFragment;
import com.example.tasktrack.models.PomodoroTimer;
import com.example.tasktrack.models.StatisticLog;
import com.example.tasktrack.models.StopWatch;
import com.example.tasktrack.models.Task;
import com.example.tasktrack.models.TimerMode;
import com.example.tasktrack.models.TimerStatus;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;


//TODO: удалить везде эти комментарии и Created by. По шапке можно получить)
// TODO: Потом приложение на git лучше перезалить сбросив историю изменений. Если не знаешь как - уточни у меня, созвонимся или еще как и обьясню
public class TimerActivity extends AppCompatActivity implements TimerSelectionDialogFragment.TimerSelectionDialogListener {
    private static final long VIBRATE_DURATION = 500;
    private static final String LOGTAG = "TimerActivity";
    private static final String CHANNEL_ID = "Timer_Notification";

    private boolean isInForegroundMode = false;

    private int workDuration;
    private int breakDuration;
    private int longBreakDuration;
    private int longBreakInterval;
    private boolean longBreakEnabled;
    private boolean vibrationEnabled;
    private boolean notificationEnabled;
    private DataSource dataSource;
    private SharedPreferences sharedPreferences;

    private Task task;
    private TimerMode currentSelectedTimerMode = TimerMode.ASK;
    private TimerMode timerMode = TimerMode.ASK;
    private TimerStatus status = TimerStatus.WAIT_FOR_WORK;
    private StopWatch stopWatch;
    private PomodoroTimer pomodoroTimer;

    private Chronometer tvTimeChrono;
    private TextView tvTimeSubtitle;
    private TextView tvTaskStatus;
    private FloatingActionButton fabStartPause;
    private FloatingActionButton fabStop;
    private long startTimeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        //create the notification channel for the app
        createNotificationChannel();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dataSource = new DataSource(this);
        fabStartPause = (FloatingActionButton) findViewById(R.id.fabStartPause);
        fabStop = (FloatingActionButton) findViewById(R.id.fabStop);

        loadSharedPreferences();
        currentSelectedTimerMode = TimerMode.values()[timerMode.ordinal()];

        task = dataSource.getTask(getIntent().getExtras().getLong("taskId"));

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvTimeChrono = (Chronometer) findViewById(R.id.tvTimeChrono);
        tvTimeSubtitle = (TextView) findViewById(R.id.tvTimeSubtitle);
        tvTaskStatus = (TextView) findViewById(R.id.tvTaskStatus);
        Chronometer dummyChronometer = (Chronometer) findViewById(R.id.dummyChronometer);
        final TextView tvTaskName = (TextView) findViewById(R.id.tvTaskName);
        final TextView tvTaskDescription = (TextView) findViewById(R.id.tvTaskDescription);
        if (task != null) {
            tvTaskName.setText(task.getName());
            tvTaskDescription.setText(task.getDescription());
            tvTaskStatus.setText(getStatusText(task));
        }

        if (timerMode == TimerMode.POMODORO) {
            long millis = TimeUnit.MINUTES.toMillis(workDuration);
            tvTimeChrono.setText(PomodoroTimer.getTimeString(millis));
        }

        stopWatch = new StopWatch(tvTimeChrono);
        pomodoroTimer = new PomodoroTimer(dummyChronometer, progressBar, tvTimeChrono);
        initPomodoro();
        initStopWatch();

        fabStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentSelectedTimerMode) {
                    case ASK:
                        showTimerModeSelectionDialog();
                        break;
                    case STOP_WATCH:
                        handleStartPauseStopWatch();
                        break;
                    case POMODORO:
                        handleStartPausePomodoro();
                        break;
                }
            }
        });

        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentSelectedTimerMode) {
                    case ASK:
                        getCurrentSelectedMode();
                        break;
                    case STOP_WATCH:
                        status = TimerStatus.WAIT_FOR_WORK;
                        setFABIcon(fabStartPause, R.drawable.ic_play_arrow_white_48dp);
                        stopWatch.stop();
                        getCurrentSelectedMode();
                        break;
                    case POMODORO:
                        if (status == TimerStatus.WORK) {
                            dialogStopPomodoro();
                        } else if (status == TimerStatus.PAUSED_WORK
                                || status == TimerStatus.BREAK
                                || status == TimerStatus.WAIT_FOR_BREAK
                                || status == TimerStatus.WAIT_FOR_WORK) {
                            stopPomodoro();
                        } else {
                            Toast.makeText(TimerActivity.this, "No timer running.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void getCurrentSelectedMode() {
        currentSelectedTimerMode = TimerMode.values()[timerMode.ordinal()];
    }

    private void stopPomodoro() {
        status = TimerStatus.WAIT_FOR_WORK;
        tvTimeSubtitle.setText(R.string.activity_timer_subtitle_work);
        setFABIcon(fabStartPause, R.drawable.ic_play_arrow_white_48dp);
        pomodoroTimer.stop();
        updateTask(task);
        createStatisticLog("Work", "Work time is finished", 1000, 500);
        getCurrentSelectedMode();
    }

    private void dialogStopPomodoro() {

        AlertDialog.Builder alert = new AlertDialog.Builder(TimerActivity.this);
        alert.setTitle(getString(R.string.dialog_stop_pomodoro_title))
                .setMessage(getString(R.string.dialog_stop_pomodoro_message))
                .setPositiveButton(getString(R.string.positive_button_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopPomodoro();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.positive_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alert.show();
    }
    private void initPomodoro() {
        updatePomodoroSettings();
        pomodoroTimer.setBreakTimerEvents(new PomodoroTimer.CountDownTimerEvent() {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                tvTimeSubtitle.setText(R.string.pomodoro_break_up);

                setFABIcon(fabStartPause, R.drawable.ic_play_arrow_white_48dp);
                if (vibrationEnabled) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(VIBRATE_DURATION);
                }

                if (notificationEnabled && !isInForeground()) {
                }

                status = TimerStatus.WAIT_FOR_WORK;
            }
        });

        pomodoroTimer.setWorkTimerEvents(new PomodoroTimer.CountDownTimerEvent() {
            @Override
            public void onTick(long millisUntilFinished) {
                task.setTimeDone(task.getTimeDone() + 1);
                updateTask(task);

                notifiyTimer("Task Tracker", "Time until break: " + PomodoroTimer.getTimeString(millisUntilFinished), task.getId(), pomodoroTimer.getProgressBar().getProgress());
            }

            @Override
            public void onFinish() {
                task.setTimeDone(task.getTimeDone() + 1);
                updateTask(task);
                createStatisticLog("Work", "Work time finished.", 1000, 500);

                tvTimeSubtitle.setText(R.string.pomodoro_work_time_up);

                if (notificationEnabled && !isInForeground()) {
                    notifiyTimer(getString(R.string.pomodoro_work_time_up), "Task: " + task.getName(), task.getId(), 500);
                } else {
                    notificationManager.cancel(workTimerNotificationId);
                }

                setFABIcon(fabStartPause, R.drawable.ic_free_breakfast_white_48dp);
                if (vibrationEnabled) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(VIBRATE_DURATION);
                }
                status = TimerStatus.WAIT_FOR_BREAK;
            }
        });

        pomodoroTimer.setOverflowStopWatchEvent(new PomodoroTimer.OverflowStopWatchEvent() {
            @Override
            public void onTick(long baseTime, long difference) {
                task.setTimeDone(task.getTimeDone() + difference);
                updateTask(task);
            }
        });
    }

    private void updatePomodoroSettings() {
        pomodoroTimer.setBreakDuration(breakDuration);
        pomodoroTimer.setWorkDuration(workDuration);
        pomodoroTimer.setLongBreakDuration(longBreakDuration);
        pomodoroTimer.setLongBreakInterval(longBreakInterval);
        pomodoroTimer.setLongBreakEnabled(longBreakEnabled);
    }

    private void loadSharedPreferences() {
        sharedPreferences = getSharedPreferences(SettingsActivityFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        timerMode = fromString(sharedPreferences.getString("timer_mode", "ask"));
        workDuration = sharedPreferences.getInt("work_duration", 25);
        breakDuration = sharedPreferences.getInt("break_duration", 5);
        longBreakInterval = sharedPreferences.getInt("long_break_interval", 4);
        longBreakDuration = sharedPreferences.getInt("long_break_duration", 15);
        longBreakEnabled = sharedPreferences.getBoolean("long_break_enabled", false);
        vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true);
        notificationEnabled = sharedPreferences.getBoolean("notification_enabled", false);

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "work_duration":
                        workDuration = sharedPreferences.getInt("work_duration", 25);
                        break;
                    case "break_duration":
                        breakDuration = sharedPreferences.getInt("break_duration", 5);
                        break;
                    case "time_mode":
                        timerMode = fromString(sharedPreferences.getString("timer_mode", null));
                }
                updatePomodoroSettings();
            }
        });
    }
    private void initStopWatch() {
        stopWatch.setOnBeforeStartListener(new StopWatch.OnBeforeStartListener() {
            @Override
            public void onBeforeStart() {
                startTimeValue = task.getTimeDone();
            }
        });
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                task.setTimeDone(startTimeValue + (stopWatch.getCurrentMeasuredTime() / 1000));
                updateTask(task);
            }
        });
    }

    private void handleStartPauseStopWatch() {
        if (status == TimerStatus.WAIT_FOR_WORK) {
            status = TimerStatus.WORK;
            tvTimeSubtitle.setText(R.string.stopwatch_mode_subtitle);
            setFABIcon(fabStartPause, R.drawable.ic_pause_white_48dp);
            stopWatch.start();
        } else if (status == TimerStatus.WORK) {
            status = TimerStatus.PAUSED_WORK;
            setFABIcon(fabStartPause, R.drawable.ic_play_arrow_white_48dp);
            stopWatch.pause();
            tvTimeChrono.startAnimation(PomodoroTimer.getBlinkAnimation());
        } else if (status == TimerStatus.PAUSED_WORK) {
            status = TimerStatus.WORK;
            setFABIcon(fabStartPause, R.drawable.ic_pause_white_48dp);
            tvTimeChrono.clearAnimation();
            stopWatch.start();
        }
    }

    private void handleStartPausePomodoro() {
        if (status == TimerStatus.WAIT_FOR_WORK) {
            status = TimerStatus.WORK;
            tvTimeSubtitle.setText(R.string.activity_timer_subtitle_work);
            setFABIcon(fabStartPause, R.drawable.ic_pause_white_48dp);
            pomodoroTimer.startWork();
        } else if (status == TimerStatus.WAIT_FOR_BREAK) {
            status = TimerStatus.BREAK;
            tvTimeSubtitle.setText(R.string.pomodoro_break_time);
            setFABIcon(fabStartPause, R.drawable.ic_skip_next_white_48dp);
            pomodoroTimer.startBreak();
        } else if (status == TimerStatus.BREAK) {
            pomodoroTimer.skipBreak();

            status = TimerStatus.WORK;
            tvTimeSubtitle.setText(R.string.activity_timer_subtitle_work);
            setFABIcon(fabStartPause, R.drawable.ic_pause_white_48dp);
        } else if (status == TimerStatus.WORK) {
            status = TimerStatus.PAUSED_WORK;
            dataSource.updateTask(task);
            setFABIcon(fabStartPause, R.drawable.ic_play_arrow_white_48dp);
            pomodoroTimer.pauseWork();
        } else if (status == TimerStatus.PAUSED_WORK) {
            status = TimerStatus.WORK;
            setFABIcon(fabStartPause, R.drawable.ic_pause_white_48dp);
            pomodoroTimer.resumeWork();
        } else {
            Toast.makeText(TimerActivity.this, "Timer already running.", Toast.LENGTH_SHORT).show();
        }
        tvTimeChrono.setAnimation(null);
    }

    private void createStatisticLog(String action, String message, long workTime, long breakTime) {
        StatisticLog statisticLog = new StatisticLog();
        statisticLog.setTask(task);
        statisticLog.setAction(action);
        statisticLog.setMessage("Something");
        statisticLog.setWorkTime(workTime);
        statisticLog.setBreakTime(breakTime);
        dataSource.createStatisticLog(statisticLog);
    }

    public static String getStatusText(Task task) {
        return String.format("%s затрачено", getFriendlyTimeString(TimeUnit.SECONDS.toMillis(task.getTimeDone()), false, true));
    }

    private TimerMode fromString(String mode) {
        TimerMode modus;
        switch (mode) {
            case "ask":
                modus = TimerMode.ASK;
                break;
            case "stopwatch":
                modus = TimerMode.STOP_WATCH;
                break;
            case "pomodoro":
                modus = TimerMode.POMODORO;
                break;
            default:
                modus = TimerMode.ASK;
                break;
        }
        return modus;
    }

    int workTimerNotificationId = 1;
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;

    private void updateTimerNotification(String description, int percent) {
        builder.setContentText(description)
                .setProgress(500, percent, false);
        notificationManager.notify(workTimerNotificationId, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notifiyTimer(String title, String description, long taskId, int percent) {

        if (builder != null) {
            updateTimerNotification(description, percent);
            return;
        }
        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description)
                .setProgress(500, 0, true);

        Intent resultIntent = new Intent(this, TimerActivity.class);
        resultIntent.putExtra("taskId", taskId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(TimerActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setAutoCancel(true);
        notificationManager.notify(workTimerNotificationId, builder.build());
    }

    private void updateTask(Task task) {
        tvTaskStatus.setText(getStatusText(task));
        dataSource.updateTask(task);
    }

    private void setFABIcon(FloatingActionButton fab, int resourceId) {
        setFABIcon(fab, resourceId, getApplicationContext());
    }

    private void setFABIcon(FloatingActionButton fab, int resourceId, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(resourceId, context.getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(resourceId));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, TaskListActivity.REQUEST_CODE_SETTINGS);
                return true;
            case R.id.action_statistic:
                Intent intent2 = new Intent(this, StatisticsActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_exit:
                this.finish();
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private static String getFriendlyTimeString(long millis, boolean showAll, boolean showSeconds) {
        String hms;
        if (TimeUnit.MILLISECONDS.toHours(millis) >= 1 || showAll && !showSeconds) {
            hms = String.format("%02dh %02dm %02ds", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        } else if (!showSeconds) {
            hms = String.format("%02dh %02dm", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1));
        } else {
            hms = String.format("%01dm %01ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        }

        return hms;
    }

    public void showTimerModeSelectionDialog() {
        DialogFragment dialog = new TimerSelectionDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public void onDialogItemSelect(DialogFragment dialog, String selectedTimerMode) {
        currentSelectedTimerMode = fromString(selectedTimerMode);
        switch (currentSelectedTimerMode) {
            case ASK:
                break;
            case STOP_WATCH:
                handleStartPauseStopWatch();
                break;
            case POMODORO:
                handleStartPausePomodoro();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForegroundMode = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForegroundMode = true;
    }

    public boolean isInForeground() {
        return isInForegroundMode;
    }
}