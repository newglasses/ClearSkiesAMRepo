package com.example.newglasses.clearskiesam;

/**
 * Created by newglasses on 02/08/2016.
 */

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimeDialogHandler extends DialogFragment {

    // when the timepicker is invoked, this method is invoked
    // the result then goes to the AlarmTimeSettings class

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // gets current time of the system
        Calendar calendar = Calendar.getInstance();

        // get current hour and minute
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // create object of time picker dialog
        TimePickerDialog timePickerDialog;

        // to handle the events of the time picker dialog use AlarmTimeSettings class

        AlarmTimeSettings alarmTimeSettings = new AlarmTimeSettings(getActivity());

        // params are the context, then the object that will be called, the hour and minute and then the format
        timePickerDialog = new TimePickerDialog(getActivity(), alarmTimeSettings, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

        return timePickerDialog;

    }
}
