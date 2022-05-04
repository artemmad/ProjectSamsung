package com.example.tasktrack.activities;

import android.content.Intent;
import android.content.res.Resources;
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
import java.util.concurrent.TimeUnit;

public class EditTaskActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DataSource dataSource;
    private String selectedColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dataSource = new DataSource(this);

        final Task editTask = dataSource.getTask(getIntent().getExtras().getLong("taskId"));

        final EditText taskName = (EditText) findViewById(R.id.editTextTaskName);
        final EditText description = (EditText) findViewById(R.id.editTextDescription);
        final EditText estTime = (EditText) findViewById(R.id.editTextEstTime);
        final EditText estTimeHours = (EditText) findViewById(R.id.editTextEstTimeHours);
        final EditText doneTimeMinutes = (EditText) findViewById(R.id.editTextTimeDone);
        final EditText doneTimeHours = (EditText) findViewById(R.id.editTextTimeDoneHours);
        final Spinner colorSpinner = (Spinner) findViewById(R.id.spinnerColor);

        taskName.setText(editTask.getName());
        description.setText(editTask.getDescription());
        estTime.setText(String.valueOf((int) TimeUnit.SECONDS.toHours(editTask.getTimeEstaminated())));
        estTimeHours.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(editTask.getTimeEstaminated() % 3600)));

        doneTimeHours.setText(String.valueOf((int) TimeUnit.SECONDS.toHours(editTask.getTimeDone())));
        doneTimeMinutes.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(editTask.getTimeDone() % 3600)));

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.background_colors, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(spinnerAdapter);
        colorSpinner.setOnItemSelectedListener(this);
        // Get selected color value for spinner
        Resources res = getResources();
        String[] answers = res.getStringArray(R.array.background_colors_values);
        selectedColor = "#" + Integer.toHexString(editTask.getColor()).toUpperCase();
        for (int i = 0; i < answers.length; i++) {
            if (answers[i].toUpperCase().equals(selectedColor)) {
                colorSpinner.setSelection(i);
            }
        }

        Button btnSave = (Button) findViewById(R.id.buttonSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (taskName.getText().toString().trim().isEmpty()) {
                    taskName.setError(getString(R.string.error_no_name));
                    return;
                } else {
                    editTask.setName(taskName.getText().toString());
                }

                editTask.setDescription(description.getText().toString());

                editTask.setSubTasks(new ArrayList<SubTask>());
                /*for (String subtask : subTasks) {
                    task.getSubTasks().add(new SubTask(subtask));
                }*/
                if (!estTime.getText().toString().isEmpty() || !estTimeHours.getText().toString().isEmpty()) {
                    int minutes = 0;
                    if (!estTime.getText().toString().isEmpty()) {
                        minutes = Integer.valueOf(estTime.getText().toString());
                    }

                    int hours = 0;
                    if (!estTime.getText().toString().isEmpty()) {
                        hours = Integer.valueOf(estTimeHours.getText().toString());
                    }
                    editTask.setTimeEstaminated(hours * 3600 + minutes * 60);
                } else {
                    editTask.setTimeEstaminated(0);
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
                    editTask.setTimeDone(hours * 3600 + minutes * 60);
                } else {
                    editTask.setTimeDone(0);
                }

                editTask.setColor(Color.parseColor(selectedColor));

                // Update task
                dataSource.updateTask(editTask);

                // Create intent and set result
                Intent data = new Intent();
                data.putExtra("taskId", editTask.getId());
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
