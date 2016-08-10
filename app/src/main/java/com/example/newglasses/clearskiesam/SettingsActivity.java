package com.example.newglasses.clearskiesam;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener{

    SharedPreferences sharedPrefs;

    // for logging
    private final String LOG_TAG = SettingsActivity.class.getSimpleName();

    // create an alarm setting that is clickable. Opens a TimePickerDialog
    private Preference timePickerButton;
    private TimePickerDialog timePickerDialog;

    // Create instance of calendar to access current device time
    private Calendar dateAndTime = Calendar.getInstance();

    // int variables to store the selected time
    private int hour, minute;
    // String variable to store the selected time - I THINK THERE IS A BETTER WAY TO DO THIS ***
    private String stringHourMinute = hour + ":" + minute;

    // create an Editor instance of SharedPreferences to add selected time to
    // https://guides.codepath.com/android/Storing-and-Accessing-SharedPreferences
    // SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    // SharedPreferences.Editor sharedPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // setupActionBar();

        // Even though this method is deprecated it is still the best to use
        // because we are targetting devices as early as Gingerbread
        // taken from Android course
        addPreferencesFromResource(R.xml.pref_general);

        // Create a layout that places a button before the PreferenceActivity preference list
        // setContentView(R.layout.pref_custom);

        // For all prefs, attach an onPreferenceChangeListener
        // so the UI summary can be updated when the preference changes
        // see the method itself (below) for the listener that it contains
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_time_picker_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gps_key)));
        // bindPreferenceSummaryToValue(findPreference(get(R.string.pref_aurora_key)));
        // bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_ISS_key)));


        timePickerButton = (Preference) findPreference("timePicker");
        timePickerButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                chooseTime(getCurrentFocus());
                return false;
            }
        });


    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };



    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {

            // for list preferences look up the correct display value in
            // the preferences entries list (since they have separate labels/ values

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                // here the settings UI is defined
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            } else {
                // For other preferences, set the summary to the value's simple String representation
                // here the settings UI is defined
                preference.setSummary(stringValue);
            }
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public static class MyPreferenceFragment extends FragmentActivity {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //addPreferencesFromResource(R.xml.preferences);
        }


        public void showDialogHandler(View view) {

            TimeDialogHandler timeDialogHandler = new TimeDialogHandler();
            timeDialogHandler.show(getSupportFragmentManager(), "timePicker");
        }
    }

    public void chooseTime(View v) {

        timePickerDialog = new TimePickerDialog(this, timePickerListener,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE),
                true);

        timePickerDialog.show();
    }

    TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int selectedHour,
                              int selectedMinute) {

            // set selected time into time picker
            dateAndTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            dateAndTime.set(Calendar.MINUTE, selectedMinute);

            hour = selectedHour;
            minute = selectedMinute;
            String minutes = Integer.toString(selectedMinute);

            // formatting minutes for the Settings Summary Display
            if (minutes.length() < 2) {
                minutes = "0" + minute;
                stringHourMinute = hour + " : " + minutes;
            } else {
                stringHourMinute = hour + " : " + minute;
            }

            sharedPrefs.edit().putString("timePicker", stringHourMinute).apply();
            sharedPrefs.edit().putInt("selectedHour", selectedHour).apply();
            sharedPrefs.edit().putInt("selectedMinute", selectedMinute).apply();

            // doing this because the listener does not pick up this change to shared prefs and
            // update the prefs ui summary automatically
            // not the right fix i think, but it works
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_time_picker_key)));

            Log.e(LOG_TAG, "Updated time pref " + stringHourMinute);
            Log.e(LOG_TAG, "Updated time in SharedPrefs " + sharedPrefs.getString("timePicker", ""));

            // do an if statement - if current time > new set time wait until tomorrow??
            /*
            Intent newIntent = new Intent (SettingsActivity.this, MainActivity.class);
            startActivity(newIntent);
            */

            Toast.makeText(SettingsActivity.this, "Selected Alarm Time: " + stringHourMinute,
                    Toast.LENGTH_LONG).show();

            WakefulIntentService.scheduleAlarms(new DailyListener(), SettingsActivity.this, false);

            // WORKS:
            //AlarmTime alarmTime = new AlarmTime();
            //alarmTime.setAlarm(SettingsActivity.this);

        }
    };

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }
}
