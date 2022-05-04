package com.example.tasktrack.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Mathias Nigsch on 18/02/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "TASKTRACKER";

    private static final String DATABASE_NAME = "tasktracker.db";
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE_TASKS = "CREATE TABLE " + TaskTrackerContract.TaskEntry.TABLE_NAME +
            "("
            + TaskTrackerContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_NAME + " TEXT " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_COLOR + " INTEGER " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_DESC + " TEXT " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_TIME_EST + " NUMERIC " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_TIME_DONE + " NUMERIC " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_ARCHIVED + " INTEGER DEFAULT 0 " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_STAMP_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP " + ","
            + TaskTrackerContract.TaskEntry.COLUMN_NAME_IS_DONE + " INTEGER DEFAULT 0"
            + ");";

    private static final String DATABASE_CREATE_STATISTICS = "CREATE TABLE " + TaskTrackerContract.StatisticLogEntry.TABLE_NAME +
            "("
            + TaskTrackerContract.StatisticLogEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + ","
            + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_TASK + " INTEGER " + ","
            + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP " + ","
            + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_ACTION + "TEXT" + ","
            + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_MESSAGE + " TEXT " + ","
            + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_BREAK_TIME + " NUMERIC " + ","
            + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_WORK_TIME + " NUMERIC " + ","
            + "FOREIGN KEY(" + TaskTrackerContract.StatisticLogEntry.COLUMN_NAME_TASK + ") REFERENCES " + TaskTrackerContract.TaskEntry.TABLE_NAME + "(" + TaskTrackerContract.TaskEntry._ID + ") ON DELETE CASCADE "
            + ");";

    private static final String DATABASE_DELETE_STATISTICS =
            "DROP TABLE IF EXISTS " + TaskTrackerContract.StatisticLogEntry.TABLE_NAME;

    private static final String DATABASE_DELETE_TASKS =
            "DROP TABLE IF EXISTS " + TaskTrackerContract.TaskEntry.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_TASKS);
        db.execSQL(DATABASE_CREATE_STATISTICS);
        Log.i(LOGTAG, "Created database.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

//        if (oldVersion == 2) {
//            db.execSQL("ALTER TABLE " + TaskEntry.TABLE_NAME + " ADD COLUMN " + TaskEntry.COLUMN_NAME_STAMP_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP ;");
//        } else {
        // Create a way to update instead of delete database
        db.execSQL(DATABASE_DELETE_TASKS);
        db.execSQL(DATABASE_DELETE_STATISTICS);
        onCreate(db);
        // }

    }
}
