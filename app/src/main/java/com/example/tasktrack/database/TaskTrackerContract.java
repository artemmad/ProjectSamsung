package com.example.tasktrack.database;

import android.provider.BaseColumns;

/**
 * Created by Mathias Nigsch on 18/02/2016.
 */
public final class TaskTrackerContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public TaskTrackerContract() {
    }

    /**
     * Represents columns for the task model.
     */
    public static abstract class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "task";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESC = "description";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_TIME_EST = "timeEstimated";
        public static final String COLUMN_NAME_TIME_DONE = "timeDone";
        public static final String COLUMN_NAME_ARCHIVED = "archived";
        public static final String COLUMN_NAME_STAMP_CREATED = "stampCreated";
        public static final String COLUMN_NAME_IS_DONE = "isDone";

        public static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_NAME_NAME,
                COLUMN_NAME_DESC,
                COLUMN_NAME_COLOR,
                COLUMN_NAME_TIME_DONE,
                COLUMN_NAME_TIME_EST,
                COLUMN_NAME_ARCHIVED,
                COLUMN_NAME_STAMP_CREATED,
                COLUMN_NAME_IS_DONE};
    }

    /**
     * Represents columns for the sub task model.
     */
    public static abstract class SubTaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "subtask";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_IS_DONE = "isDone";
        public static final String COLUMN_NAME_PARENT_TASK = "taskId";

        public static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_NAME_NAME,
                COLUMN_NAME_PARENT_TASK,
                COLUMN_NAME_IS_DONE};
    }

    /**
     * Represents columns for the statistic log model.
     */
    public static abstract class StatisticLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "statistics";
        public static final String COLUMN_NAME_TASK = "taskId";
        public static final String COLUMN_NAME_TIME = "timeStamp";
        public static final String COLUMN_NAME_ACTION = "action";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_WORK_TIME = "workTime";
        public static final String COLUMN_NAME_BREAK_TIME = "breakTime";
        public static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_NAME_TASK,
                COLUMN_NAME_TIME,
                COLUMN_NAME_ACTION,
                COLUMN_NAME_MESSAGE,
                COLUMN_NAME_WORK_TIME,
                COLUMN_NAME_BREAK_TIME
        };
    }
}
