package com.example.tasktrack.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;


import androidx.fragment.app.DialogFragment;

import com.example.tasktrack.R;

/**
 * Created by Mathias Nigsch on 22/02/2016.
 */
public class TimerSelectionDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface TimerSelectionDialogListener {
        void onDialogItemSelect(DialogFragment dialog, String selectedTimerMode);
    }

    // Use this instance of the interface to deliver action events
    private TimerSelectionDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the TimerSelectionDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the TimerSelectionDialogListener so we can send events to the host
            listener = (TimerSelectionDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TimerSelectionDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_timer_mode)
                .setIcon(R.drawable.ic_icon_task)  // TODO: 24/02/2016  add a better icon
                .setItems(R.array.timer_mode_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] some_array = getResources().getStringArray(R.array.timer_mode_dialog_items_values);
                        String selectedMode = some_array[which];
                        listener.onDialogItemSelect(TimerSelectionDialogFragment.this, selectedMode);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}