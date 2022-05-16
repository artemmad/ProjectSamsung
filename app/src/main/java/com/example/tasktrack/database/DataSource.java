package com.example.tasktrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.tasktrack.models.StatisticLog;
import com.example.tasktrack.models.SubTask;
import com.example.tasktrack.models.Task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.example.tasktrack.database.TaskTrackerContract.StatisticLogEntry;
import com.example.tasktrack.database.TaskTrackerContract.TaskEntry;
public class DataSource {

    private static final String LOGTAG = "TASKTRACKER";

    private SQLiteDatabase db;
    private final SQLiteOpenHelper helper;

    public DataSource(Context context) {
        // Create or access db
        helper = new DBHelper(context);
    }

    public void open() {
        Log.i(LOGTAG, "Database opened");
        db = helper.getWritableDatabase();
    }

    public void close() {
        Log.i(LOGTAG, "Database closed");
        helper.close();
    }

    public Task createTask(Task task) {
        if (task == null) {
            Log.w(LOGTAG, "Could not create task, task is null.");
            return null;
        }

        open();

        ContentValues values = taskToContentValues(task);

        long insertId = db.insert(TaskEntry.TABLE_NAME, null, values);
        task.setId(insertId);

        //TODO
        // Create sub tasks
//        if (task.getSubTasks() != null) {
//            for (int i = 0; i < task.getSubTasks().size(); i++) {
//                SubTask currentSubTask = task.getSubTasks().get(i);
//                task.getSubTasks().set(i, createSubTask(currentSubTask));
//            }
//        }
        close();

        return task;

    }

    public SubTask createSubTask(SubTask subTask) {
        ContentValues values = new ContentValues();
        values.put(TaskTrackerContract.SubTaskEntry.COLUMN_NAME_NAME, subTask.getName());
        values.put(TaskTrackerContract.SubTaskEntry.COLUMN_NAME_PARENT_TASK, subTask.getParent().getId());
        values.put(TaskTrackerContract.SubTaskEntry.COLUMN_NAME_IS_DONE, subTask.isDone());

        long insertId = db.insert(TaskTrackerContract.SubTaskEntry.TABLE_NAME, null, values);
        subTask.setId(insertId);
        return subTask;
    }

    public List<Task> findAllTasks() {
        open();

        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.query(TaskEntry.TABLE_NAME, TaskEntry.ALL_COLUMNS, null, null, null, null, null);

        Log.i(LOGTAG, "Retrieved " + cursor.getCount() + " task entries.");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Task task = cursorToTask(cursor);
                if (task != null) {
                    tasks.add(task);
                }
            }
        }

        close();

        return tasks;
    }

    public static Task cursorToTask(Cursor cursor) {
        try {
            Task task = new Task();
            task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry._ID)));
            task.setName(cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_NAME)));
            task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DESC)));
            task.setTimeEstaminated(cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TIME_EST)));
            task.setTimeDone(cursor.getLong(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TIME_DONE)));
            task.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COLOR)));
            task.setCreatedTime(Timestamp.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_STAMP_CREATED))));
            task.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_ARCHIVED)) == 1);
            task.setDone(cursor.getInt(cursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_IS_DONE)) == 1);
            return task;
        } catch (CursorIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private ContentValues taskToContentValues(Task task) {
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME_NAME, task.getName());
        values.put(TaskEntry.COLUMN_NAME_DESC, task.getDescription());
        values.put(TaskEntry.COLUMN_NAME_COLOR, task.getColor());
        values.put(TaskEntry.COLUMN_NAME_TIME_EST, task.getTimeEstaminated());
        values.put(TaskEntry.COLUMN_NAME_TIME_DONE, task.getTimeDone());
        values.put(TaskEntry.COLUMN_NAME_ARCHIVED, task.isArchived());
        values.put(TaskEntry.COLUMN_NAME_IS_DONE, task.isDone());
        return values;
    }

    public Cursor getAllTasksCursor() {
        return getAllTasksCursor(false);
    }

    public Cursor getAllTasksCursor(boolean withArchived) {
        open();

        String[] selection = null;
        String where = "";

        if (!withArchived) {
            where = " WHERE " + TaskEntry.COLUMN_NAME_ARCHIVED + " = ?";
            selection = new String[]{"0"};
        }

        //Cursor cursor = db.query(TaskEntry.TABLE_NAME, TaskEntry.ALL_COLUMNS, null, null, null, null, null);
        Cursor cursor = db.rawQuery(
                "SELECT " +
                        TaskEntry._ID + " AS _id," +
                        TaskEntry.COLUMN_NAME_DESC + ", " +
                        TaskEntry.COLUMN_NAME_TIME_EST + ", " +
                        TaskEntry.COLUMN_NAME_TIME_DONE + ", " +
                        TaskEntry.COLUMN_NAME_COLOR + ", " +
                        TaskEntry.COLUMN_NAME_IS_DONE + ", " +
                        TaskEntry.COLUMN_NAME_ARCHIVED + ", " +
                        TaskEntry.COLUMN_NAME_STAMP_CREATED + ", " +
                        TaskEntry.COLUMN_NAME_NAME +
                        " FROM " + TaskEntry.TABLE_NAME + where, selection);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        close();
        return cursor;
    }

    public Task getTask(long id) {
        open();

        String where = TaskEntry._ID + "=?";
        String[] args = new String[]{Long.toString(id)};
        Cursor cursor = db.query(TaskEntry.TABLE_NAME, TaskEntry.ALL_COLUMNS, where, args, null, null, null);

        cursor.moveToFirst();
        Task task = cursorToTask(cursor);
        cursor.close();
        close();

        return task;
    }

    public int updateTask(Task task) {
        open();

        ContentValues values = taskToContentValues(task);

        // Which row to update, based on the ID
        String selection = TaskEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(task.getId())};

        int count = db.update(
                TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        close();

        return count;
    }

    public int deleteTask(long id) {
        open();
        int result = db.delete(TaskEntry.TABLE_NAME, TaskEntry._ID + " =?", new String[]{Long.toString(id)});
        close();
        return result;
    }

    public StatisticLog createStatisticLog(StatisticLog statisticLog) {
        if (statisticLog == null) {
            Log.w(LOGTAG, "Could not create statistic log: is null.");
            return null;
        }

        open();

        ContentValues values = statisticLogToContentValues(statisticLog);

        long insertId = db.insert(TaskTrackerContract.StatisticLogEntry.TABLE_NAME, null, values);
        statisticLog.setId(insertId);
        close();

        return statisticLog;
    }

    private ContentValues statisticLogToContentValues(StatisticLog statisticLog) {
        ContentValues values = new ContentValues();
        if (statisticLog.getTask() != null) {
            values.put(StatisticLogEntry.COLUMN_NAME_TASK, statisticLog.getTask().getId());
        }
        values.put(StatisticLogEntry.COLUMN_NAME_ACTION, statisticLog.getAction());
        values.put(StatisticLogEntry.COLUMN_NAME_MESSAGE, statisticLog.getMessage());
        values.put(StatisticLogEntry.COLUMN_NAME_BREAK_TIME, statisticLog.getBreakTime());
        values.put(StatisticLogEntry.COLUMN_NAME_WORK_TIME, statisticLog.getWorkTime());
        return values;
    }

    public StatisticLog getStatisticLog(long id) {
        open();

        Cursor cursor = db.rawQuery(
                "SELECT " + "* " +
                        " FROM " + TaskEntry.TABLE_NAME +
                        " INNER JOIN " + StatisticLogEntry.TABLE_NAME +
                        " ON " + TaskEntry.TABLE_NAME + "." + TaskEntry._ID + " = " + StatisticLogEntry.TABLE_NAME + "." + StatisticLogEntry.COLUMN_NAME_TASK +
                        " WHERE " + TaskEntry.TABLE_NAME + "." + TaskEntry._ID + " = " + id
                , null);

        cursor.moveToFirst();
        StatisticLog statisticLog = cursorToStatisticLog(cursor);
        cursor.close();
        close();

        return statisticLog;
    }

    public static StatisticLog cursorToStatisticLog(Cursor cursor) {
        try {
            StatisticLog statisticLog = new StatisticLog();
            Task task = null;
            try {
                task = cursorToTask(cursor);

                if (task != null) {
                    task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(StatisticLogEntry.COLUMN_NAME_TASK)));
                }
            } catch (Exception e) {
                // No Task set
            }
            statisticLog.setId(cursor.getLong(cursor.getColumnIndexOrThrow(StatisticLogEntry._ID)));
            statisticLog.setAction(cursor.getString(cursor.getColumnIndexOrThrow(StatisticLogEntry.COLUMN_NAME_ACTION)));
            statisticLog.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(StatisticLogEntry.COLUMN_NAME_MESSAGE)));
            statisticLog.setBreakTime(cursor.getLong(cursor.getColumnIndexOrThrow(StatisticLogEntry.COLUMN_NAME_BREAK_TIME)));
            statisticLog.setWorkTime(cursor.getLong(cursor.getColumnIndexOrThrow(StatisticLogEntry.COLUMN_NAME_WORK_TIME)));
            statisticLog.setTime(Timestamp.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(StatisticLogEntry.COLUMN_NAME_TIME))));
            statisticLog.setTask(task);
            return statisticLog;
        } catch (CursorIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Cursor getAllStatisticLogsCursor() {
        return getAllStatisticLogsCursor(new Timestamp(0), new Timestamp(0));
    }

    //TODO: add from, to functionality
    public Cursor getAllStatisticLogsCursor(Timestamp from, Timestamp to) {
        open();

        Cursor cursor = db.rawQuery(
                "SELECT " + "* " +
                        " FROM " + TaskEntry.TABLE_NAME +
                        " INNER JOIN " + StatisticLogEntry.TABLE_NAME +
                        " ON " + TaskEntry.TABLE_NAME + "." + TaskEntry._ID + " = " + StatisticLogEntry.TABLE_NAME + "." + StatisticLogEntry.COLUMN_NAME_TASK, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        close();
        return cursor;
    }

    public int deleteStatisticLog(long id) {
        open();
        int result = db.delete(StatisticLogEntry.TABLE_NAME, StatisticLogEntry._ID + " =?", new String[]{Long.toString(id)});
        close();
        return result;
    }
}
