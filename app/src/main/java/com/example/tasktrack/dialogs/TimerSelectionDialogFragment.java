package com.example.tasktrack.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;


import androidx.fragment.app.DialogFragment;

import com.example.tasktrack.R;

public class TimerSelectionDialogFragment extends DialogFragment {
    public interface TimerSelectionDialogListener {
        void onDialogItemSelect(DialogFragment dialog, String selectedTimerMode);
    }
    private TimerSelectionDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        try {
            listener = (TimerSelectionDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TimerSelectionDialogListener");
        }
    }
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the TimerSelectionDialogListener so we can send events to the host
//            listener = (TimerSelectionDialogListener) activity;
//        } catch (ClassCastException e) {
//            // The activity doesn't implement the interface, throw exception
//            throw new ClassCastException(activity.toString()
//                    + " must implement TimerSelectionDialogListener");
//        }
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_timer_mode)
                .setIcon(R.drawable.ic_icon_task)
                .setItems(R.array.timer_mode_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] some_array = getResources().getStringArray(R.array.timer_mode_dialog_items_values);
                        String selectedMode = some_array[which];
                        listener.onDialogItemSelect(TimerSelectionDialogFragment.this, selectedMode);
                    }
                });
        return builder.create();
    }
}