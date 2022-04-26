package com.example.tasktrack.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.tasktrack.R;
import com.example.tasktrack.database.DataSource;
import com.example.tasktrack.models.SubTask;
import com.example.tasktrack.models.Task;

import java.util.ArrayList;

public class NewTaskActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private DataSource dataSource;
    private String selectedColor;

    // http://stackoverflow.com/questions/15393899/how-to-close-activity-and-go-back-to-previous-activity-in-android
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dataSource = new DataSource(this);
        // final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subTasks);
       /* final ListView listViewSubtasks = (ListView) findViewById(R.id.listViewSubtasks);
        listViewSubtasks.setAdapter(adapter);
*/
        final EditText taskName = (EditText) findViewById(R.id.editTextTaskName);
        final EditText description = (EditText) findViewById(R.id.editTextDescription);
        final EditText estTimeMinutes = (EditText) findViewById(R.id.editTextEstTime);
        final EditText estTimeHours = (EditText) findViewById(R.id.editTextEstTimeHours);
        final EditText doneTimeMinutes = (EditText) findViewById(R.id.editTextTimeDone);
        final EditText doneTimeHours = (EditText) findViewById(R.id.editTextTimeDoneHours);

        final Spinner colorSpinner = (Spinner) findViewById(R.id.spinnerColor);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.background_colors, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(spinnerAdapter);
        colorSpinner.setOnItemSelectedListener(this);

        //final EditText subTaskName = (EditText) findViewById(R.id.subtaskName);

        /*ImageButton btnAddSubtask = (ImageButton) findViewById(R.id.buttonAddSubTask);
        btnAddSubtask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.add(subTaskName.getText().toString());
                subTaskName.setText("");
            }
        });*/

        Button btnSave = (Button) findViewById(R.id.buttonSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data
                Task task = new Task();

                if (taskName.getText().toString().trim().isEmpty()) {
                    taskName.setError(getString(R.string.error_no_name));
                    return;
                } else {
                    task.setName(taskName.getText().toString());
                }

                task.setDescription(description.getText().toString());
                task.setSubTasks(new ArrayList<SubTask>());
                /*for (String subtask : subTasks) {
                    task.getSubTasks().add(new SubTask(subtask));
                }*/

                // Get estimaed time
                if (!estTimeMinutes.getText().toString().isEmpty() || !estTimeHours.getText().toString().isEmpty()) {
                    int minutes = 0;
                    if (!estTimeMinutes.getText().toString().isEmpty()) {
                        minutes = Integer.valueOf(estTimeMinutes.getText().toString());
                    }

                    int hours = 0;
                    if (!estTimeHours.getText().toString().isEmpty()) {
                        hours = Integer.valueOf(estTimeHours.getText().toString());
                    }
                    task.setTimeEstaminated(hours * 3600 + minutes * 60);
                } else {
                    task.setTimeEstaminated(0);
                }

                // Get time done
                if (!doneTimeMinutes.getText().toString().isEmpty() || !doneTimeHours.getText().toString().isEmpty()) {
                    int minutes = 0;
                    if (!doneTimeMinutes.getText().toString().isEmpty()) {
                        minutes = Integer.valueOf(doneTimeMinutes.getText().toString());
                    }

                    int hours = 0;
                    if (!doneTimeHours.getText().toString().isEmpty()) {
                        hours = Integer.valueOf(doneTimeHours.getText().toString());
                    }
                    task.setTimeDone(hours * 3600 + minutes * 60);
                } else {
                    task.setTimeDone(0);
                }

                task.setColor(Color.parseColor(selectedColor));

                // Create task in db
                task = dataSource.createTask(task);

                // Create intent and set result
                Intent data = new Intent();
                data.putExtra("taskId", task.getId());
                setResult(RESULT_OK, data);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] some_array = getResources().getStringArray(R.array.background_colors_values);
        selectedColor = some_array[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
