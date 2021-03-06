package com.example.tasktrack.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import androidx.preference.SeekBarPreference;


// Would also work:
// http://www.codeproject.com/Articles/163541/SeekBar-Preference
// http://no-magic.info/development-for-android-os/seekbar-in-preferences.html
// http://stackoverflow.com/questions/5050272/android-seekbarpreference

/**
 * Created by Mathias Nigsch on 21/02/2016.
 */
public class SeekBarPreferenceAutoSummary extends SeekBarPreference {
    private int max;
    private int min = 1;

    public SeekBarPreferenceAutoSummary(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public SeekBarPreferenceAutoSummary(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SeekBarPreferenceAutoSummary(Context context) {
        super(context);
    }

    @Override
    public int getMin() {
        return min;
    }

    @Override
    public void setMin(int min) {
        this.min = min;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public void setMax(int max) {
          super.setMax(max);
          this.max = max;
      }

    @Override
    public CharSequence getSummary() {
        String summary = (String) super.getSummary();
        return getSummary(getPersistedInt(1));
    }

//    @Override
    public CharSequence getSummary(int value) {
        String summary = (String) super.getSummary();
        if (summary != null) {
            return String.format(summary, String.valueOf(value));
        } else {
            return String.valueOf(String.valueOf(value));
        }
    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        super.onProgressChanged(seekBar, i, b);
        //setSummary(getSummary(i));

        if (i < min) {
            i = min;
            seekBar.setProgress(i);
        }

        onStopTrackingTouch(seekBar);
    }
}
