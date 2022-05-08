package com.example.tasktrack.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.example.tasktrack.R;

/**
 * Fragment for the preference view.
 */
public class SettingsActivityFragment extends PreferenceFragment {

    public static final String SETTINGS_SHARED_PREFERENCES_FILE_NAME = SettingsActivityFragment.class.getName() + ".SETTINGS_SHARED_PREFERENCES_FILE_NAME";
    private static final String LOGTAG = "TASKTRACKER";

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // http://stackoverflow.com/questions/17880437/which-settings-file-does-preferencefragment-read-write
        // Define the settings file to use by this settings fragment
        getPreferenceManager().setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Load preference objects and set the maximum (as it is not set via the preference.xml)
        Preference workDuration = findPreference("work_duration");
        Preference breakDuration = findPreference("break_duration");
        Preference longBreakDuration = findPreference("long_break_duration");
        Preference longBreakInterval = findPreference("long_break_interval");

//        workDuration.setMax(40); //TODO: удалит потом если не нужно
//        breakDuration.setMax(10);
//        longBreakDuration.setMax(20);
//        longBreakInterval.setMax(5);
    }

    //TODO: коузило ошибку. Можно будет удалить перед защитой
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_settings, container, false);
//    }
}
