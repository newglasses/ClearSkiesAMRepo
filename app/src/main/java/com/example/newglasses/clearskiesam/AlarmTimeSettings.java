package com.example.newglasses.clearskiesam;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

/**
 * Created by newglasses on 02/08/2016.
 * Implements a time set listener for the time picker dialog.
 */
public class AlarmTimeSettings implements TimePickerDialog.OnTimeSetListener {

    // context accessed here - needed to display a toast
    // we want to show the time selected as a toast

    Context context;

    public AlarmTimeSettings (Context context) {

        this.context = context;
    }

    // when the time picker dialog is used (user sets time), this method will be invoked
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


    }


}
