package com.example.tasktrack.activities;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tasktrack.R;
import com.example.tasktrack.adapters.StatisticsCursorAdapter;
import com.example.tasktrack.database.DataSource;


public class StatisticsActivity extends AppCompatActivity {

    private DataSource dataSource;
    private StatisticsCursorAdapter cursorAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dataSource = new DataSource(this);
        // Get list and set empty view
        listView = (ListView) findViewById(R.id.listViewStatistics);
        TextView emptyView = (TextView) findViewById(R.id.statistic_list_empty);
        listView.setEmptyView(emptyView);

        registerForContextMenu(listView);

        dataSource = new DataSource(this);
        cursorAdapter = new StatisticsCursorAdapter(this, dataSource.getAllStatisticLogsCursor(), 0);
        listView.setAdapter(cursorAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Updates the list view with data from the database.
     */
    private void updateStatistics() {
        new UpdateStatisticTask().execute();
    }

    private class UpdateStatisticTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... params) {
            return dataSource.getAllStatisticLogsCursor();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            cursorAdapter.changeCursor(cursor);
        }
    }

}
