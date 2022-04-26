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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tasktrack.R;
import com.example.tasktrack.activities.TimerActivity;
import com.example.tasktrack.database.DataSource;
import com.example.tasktrack.models.Task;

import java.util.concurrent.TimeUnit;

/**
 * Created by Mathias on 20/02/2016.
 */
public class TaskListCursorAdapter extends CursorAdapter implements View.OnCreateContextMenuListener {
    private static final String LOGTAG = "TASKTRACKER";
    private LayoutInflater cursorInflater;
    private Callback callback;

    public TaskListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public TaskListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.task_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder holder;
        Task task = DataSource.cursorToTask(cursor);

        final long id;
        if (task != null) {
            id = task.getId();
        } else {
            Log.e(LOGTAG, "Not able to show task because task is null.");
            return;
        }

        if (view.getTag() == null) {
            // Lookup view for data population
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Set background color of task
        //relativeLayout.setBackgroundColor(task.getColor());
        holder.populateRow(task);

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onEditButtonClick(id);
                }
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TimerActivity.class);
                intent.putExtra("taskId", id);
                context.startActivity(intent);
            }
        });

        view.setOnCreateContextMenuListener(this);
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    static class ViewHolder {
        TextView name;
        TextView timeDone;
        TextView status;
        RelativeLayout layout;
        ImageButton editButton;

        /**
         * Initialize the holder.
         *
         * @param view The view to populate the objects from.
         */
        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tvName);
            timeDone = (TextView) view.findViewById(R.id.tvTimeDone);
            status = (TextView) view.findViewById(R.id.tvStatus);
            layout = (RelativeLayout) view.findViewById(R.id.taskItemLayout);
            editButton = (ImageButton) view.findViewById(R.id.editTask);
        }

        public void populateRow(Task task) {
            // Populate the data into the template view using the data object
            name.setText(task.getName());

            // Get time done in hours and minutes
            long hours = TimeUnit.SECONDS.toHours(task.getTimeDone());
            long remainMinute = TimeUnit.SECONDS.toMinutes(task.getTimeDone()) - TimeUnit.HOURS.toMinutes(hours);
            String result = String.format("%01d", hours) + "h " + String.format("%01d", remainMinute) + "m";
            timeDone.setText(result);

            // TODO: first create table for subtasks
//            // Get subtask status
//            int amountSubTasks = 0;
//            int amountFinishedSubtasks = 0;
//            if (task.getSubTasks() != null) {
//                amountSubTasks = task.getSubTasks().size();
//
//                for (SubTask subTask : task.getSubTasks()) {
//                    if (subTask.isDone()) {
//                        amountFinishedSubtasks++;
//                    }
//                }
//            }
//
//            status.setText(String.format("%d/%d Subtasks done", amountFinishedSubtasks, amountSubTasks));

            if (task.getDescription() == null || task.getDescription().isEmpty()) {
                status.setText(R.string.task_item_description_empty);
            } else {
                status.setText(task.getDescription());
            }
            // Set opacity of task
            if (task.isDone()) {
                layout.setAlpha((float) 0.5);
                layout.setBackgroundColor(0);
            } else {
                layout.setAlpha((float) 1);
                if (task.getColor() != 0) {
                    layout.setBackgroundColor(task.getColor());
                } else {
                    layout.setBackgroundResource(R.color.bg1_task_grey);
                }
            }
        }
    }

    //http://www.c-sharpcorner.com/UploadFile/9e8439/create-custom-listener-on-button-in-listitem-listview-in-a/
    public interface Callback {
        void onEditButtonClick(long id);
    }
}
