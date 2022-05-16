package com.example.tasktrack.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.tasktrack.R;
import com.example.tasktrack.adapters.TaskListCursorAdapter;
import com.example.tasktrack.database.DataSource;
import com.example.tasktrack.models.Task;
import com.google.android.material.snackbar.Snackbar;


public class TaskListActivity extends AppCompatActivity implements TaskListCursorAdapter.Callback {
    public static final int REQUEST_CODE_SETTINGS = 1002;
    public static final int REQUEST_CODE_NEW_TASK = 100;
    private static final int REQUEST_CODE_UDPATE_TASK = 1003;
    private static final String LOGTAG = "TASKTRACKER";

    private DataSource dataSource;
    private TaskListCursorAdapter cursorAdapter;
    private ListView listViewTasks;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listViewTasks = (ListView) findViewById(R.id.listViewTasks);
        TextView emptyView = (TextView) findViewById(R.id.main_list_empty);
        listViewTasks.setEmptyView(emptyView);

        registerForContextMenu(listViewTasks);
        dataSource = new DataSource(this);
        cursorAdapter = new TaskListCursorAdapter(this, dataSource.getAllTasksCursor(), 0);
        cursorAdapter.setCallback(this);

        listViewTasks.setAdapter(cursorAdapter);
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                                updateTaskListView();
                        }
                    }
                });
        listViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TaskListActivity.this, "bla", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateTaskListView() {
        new UpdateTaskListTask().execute();
    }

    private class UpdateTaskListTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... params) {
            return dataSource.getAllTasksCursor();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            cursorAdapter.changeCursor(cursor);
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
                someActivityResultLauncher.launch(intent);
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_CODE_NEW_TASK) {
//                // int taskId = data.getExtras().getInt("taskId");
//                updateTaskListView();
//            } else if (requestCode == REQUEST_CODE_UDPATE_TASK) {
//                // int taskId = data.getExtras().getInt("taskId");
//                updateTaskListView();
//            }
//        }
//    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_task_item, menu);

        menu.setHeaderTitle("Выберите действие");
        menu.setHeaderIcon(R.drawable.ic_icon_task);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Task clickedItem = dataSource.getTask(cursorAdapter.getItemId(info.position));

        if (clickedItem.isDone()) {
            menu.findItem(R.id.context_menu_set_done).setVisible(false);
            menu.findItem(R.id.context_menu_set_undone).setVisible(true);
        } else {
            menu.findItem(R.id.context_menu_set_done).setVisible(true);
            menu.findItem(R.id.context_menu_set_undone).setVisible(false);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void deleteTask(final long taskId) {
        AlertDialog.Builder alert = new AlertDialog.Builder(
                TaskListActivity.this);
        alert.setTitle("Удалить задачу");
        alert.setMessage("Вы уверены, что хотите удалить задачу?");
        alert.setPositiveButton("ДА", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataSource.deleteTask(taskId);
                Snackbar.make(listViewTasks, "Задача удалена", Snackbar.LENGTH_LONG);
                updateTaskListView();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.context_menu_edit:
                onEditButtonClick(cursorAdapter.getItemId(itemInfo.position));
                return true;
            case R.id.context_menu_delete_item:
                long taskId = cursorAdapter.getItemId(itemInfo.position);
                deleteTask(taskId);
                return true;
            case R.id.context_menu_set_done:
                Task changedTask = DataSource.cursorToTask((Cursor) cursorAdapter.getItem(itemInfo.position));
                if (changedTask != null) {
                    changedTask.setDone(true);
                    dataSource.updateTask(changedTask);
                    updateTaskListView();
                } else {
                    Log.e(LOGTAG, "Не удалось установить задачу как выполненную, задача не найдена.");
                }
                return true;
            case R.id.context_menu_archive:
                Task changedTask2 = DataSource.cursorToTask((Cursor) cursorAdapter.getItem(itemInfo.position));
                changedTask2.setArchived(true);
                dataSource.updateTask(changedTask2);
                updateTaskListView();
                return true;
            case R.id.context_menu_duplicate:
                Task original = DataSource.cursorToTask((Cursor) cursorAdapter.getItem(itemInfo.position));
                dataSource.createTask(original);
                updateTaskListView();
                return true;
            case R.id.context_menu_set_undone:
                Task changedTask1 = DataSource.cursorToTask((Cursor) cursorAdapter.getItem(itemInfo.position));
                changedTask1.setDone(false);
                dataSource.updateTask(changedTask1);
                updateTaskListView();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataSource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataSource.close();
    }

    public void addNewTask(View view) {
        Intent intent = new Intent(this, NewTaskActivity.class);
        someActivityResultLauncher.launch(intent);
        //startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void onEditButtonClick(long id) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("taskId", id);
        someActivityResultLauncher.launch(intent);
        //startActivityForResult(intent, REQUEST_CODE_UDPATE_TASK);
    }
}