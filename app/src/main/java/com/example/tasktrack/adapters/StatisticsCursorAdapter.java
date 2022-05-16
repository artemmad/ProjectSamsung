package com.example.tasktrack.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.tasktrack.R;
import com.example.tasktrack.activities.TimerActivity;
import com.example.tasktrack.database.DataSource;
import com.example.tasktrack.models.StatisticLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StatisticsCursorAdapter extends CursorAdapter implements View.OnCreateContextMenuListener {
    private static final String LOGTAG = "TASKTRACKER";
    private LayoutInflater cursorInflater;

    public StatisticsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public StatisticsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.statistics_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder holder;
        final StatisticLog statisticLog = DataSource.cursorToStatisticLog(cursor);

        if (view.getTag() == null) {
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.populateRow(statisticLog);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statisticLog != null) {
                    Intent intent = new Intent(context, TimerActivity.class);
                    intent.putExtra("taskId", statisticLog.getTask().getId());
                    context.startActivity(intent);
                } else {
                    Log.e(LOGTAG, "An error occurred, the statistic log is null for the given view.");
                }
            }
        });

        view.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    static class ViewHolder {
        TextView taskName;
        TextView action;
        TextView message;
        TextView workTime;
        TextView breakTime;
        TextView timeStamp;

        public ViewHolder(View view) {
            action = (TextView) view.findViewById(R.id.tvAction);

            workTime = (TextView) view.findViewById(R.id.tvWorkTime);
            breakTime = (TextView) view.findViewById(R.id.tvBreakTime);
            taskName = (TextView) view.findViewById(R.id.tvTaskName);
            timeStamp = (TextView) view.findViewById(R.id.tvTimeStamp);
        }

        public void populateRow(StatisticLog statisticLog) {
            action.setText(statisticLog.getAction());
            taskName.setText(statisticLog.getTask().getName());

            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
            Date netDate = (new Date(statisticLog.getTime().getTime()));
            timeStamp.setText(sdf.format(netDate));

            long hours = TimeUnit.SECONDS.toHours(statisticLog.getWorkTime());
            long remainMinute = TimeUnit.SECONDS.toMinutes(statisticLog.getWorkTime()) - TimeUnit.HOURS.toMinutes(hours);
            String result = String.format("Work: %sh %sm", String.format("%01d", hours), String.format("%01d", remainMinute));
            workTime.setText(result);

            DateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.GERMANY);
            netDate = (new Date(statisticLog.getBreakTime() * 1000));
            breakTime.setText(String.format("Break: %sm", dateFormat.format(netDate)));
        }
    }
}
