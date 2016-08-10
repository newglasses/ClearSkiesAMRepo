package com.example.newglasses.clearskiesam;

/**
 * Created by newglasses on 02/08/2016.
 */

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public static ClearSkiesAdapter clearSkiesAdapter;

    public static ArrayAdapter <String> mClearSkiesAdapter;

    public static MatrixCursor matrixCursor;

    private Activity thisActivity;

    private ListView listView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseEventLayout;

    private static final String SELECTED_KEY = "selected_position";

    // Specify the columns we need.
    private static final String[] LISTVIEW_COLUMNS = {

            BaseColumns._ID,
            "icon",
            "first",
            "second",
            "third"
    };

    // Specify the columns we need.
    private static final int[] LISTVIEW_VIEWS = {

            R.id.list_icon,
            R.id.list_first,
            R.id.list_second,
            R.id.list_third
    };


    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {

        super.onStart();
        // updateClearSkies(thisActivity);
        // setAlarm(thisActivity);

        Log.e(LOG_TAG, "Inside onStart()");

    }

    // it's important to inflate the menu before the UI is initialised
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // menu.clear();
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            thisActivity.startService(new Intent(thisActivity, ClearSkiesService.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(LOG_TAG, "Inside onCreateView()");

        // Create the rootview
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /*

        String[] from = LISTVIEW_COLUMNS;
        int[] to = {R.id.list_first, R.id.list_second, R.id.list_third};
        matrixCursor = new MatrixCursor(from);

        matrixCursor.addRow(new Object[]{0, R.mipmap.ic_launcher, "Monkey", "Rite", "Out"});
        matrixCursor.addRow(new Object[]{1, R.mipmap.ic_launcher, "Monkey", "Rite", "Out"});
        matrixCursor.addRow(new Object[]{2, R.mipmap.ic_launcher, "Monkey", "Rite", "Out"});
        matrixCursor.addRow(new Object[]{3, R.mipmap.ic_launcher, "Monkey", "Rite", "Out"});

        Log.e(LOG_TAG, "cursor looks like: " + matrixCursor.getCount());

        clearSkiesAdapter = new ClearSkiesAdapter(thisActivity, null, 0);

        // Create the rootview
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        listView = (ListView) rootView.findViewById(R.id.listview_clearskies);

        listView.setAdapter(clearSkiesAdapter);



        // We'll call our MainActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    /*
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));


                    String moreInfo = (String) clearSkiesAdapter.getItem(position);
                    Intent intent = new Intent (thisActivity, MainActivityFragment.class)
                            .putExtra(Intent.EXTRA_TEXT, moreInfo);
                    startActivity(intent);

                    // create a toast when the list item is pressed
                    //Toast.makeText(thisActivity, moreInfo, Toast.LENGTH_SHORT).show();
                }
                mPosition = position;
            }
        });
                */



        /* If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        */

        /* now make some dummy data for the list
        String [] clearSkiesArray = {

                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - Blah - 56/78",
                "Sun - Misty - 34/43"
        };

        List<String> todayClearSkies = new ArrayList<>(
                Arrays.asList(clearSkiesArray));
        */

        /*
        String[] from = {BaseColumns._ID, "col1", "col2", "col3", "col4", "col5"};
        int[] to = {R.id.list_extra_icon, R.id.list_extra_middle_high, R.id.list_extra_middle_low,
                R.id.list_extra_right_high, R.id.list_extra_right_low};
        cursor = new MatrixCursor(from);

        cursor.addRow(new Object[]{key, R.mipmap.ic_launcher, "Hello", "Test", "This", "Out"});

        adapter = new SimpleCursorAdapter(thisActivity, R.layout.list_item_extra, cursor, from, to);
        */

        //matrixCursor.addRow(new Object[]{4, R.mipmap.ic_launcher, "Hello", "Test", "This"});

        // create an ArrayAdapter to feed the ListView
        mClearSkiesAdapter = new ArrayAdapter<String>(

                //get context
                getActivity(),
                // id of list item layout
                R.layout.list_item_extra,
                R.id.list_first,
                new ArrayList<String>());

        // get a reference to the listview and attach this adapter to it
        // adapter supplies listitem layouts to the listview based on the todayClearSkies data
        ListView listView = (ListView) rootView.findViewById(R.id.listview_clearskies);

        listView.setAdapter(mClearSkiesAdapter);

        // TRYING OUT:
        //listView.setAdapter(clearSkiesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // access the data from the array adapter
                String moreInfo = mClearSkiesAdapter.getItem(position);

                Intent intent = new Intent (thisActivity, MainActivityFragment.class)
                        .putExtra(Intent.EXTRA_TEXT, moreInfo);
                startActivity(intent);

                // create a toast when the list item is pressed
                //Toast.makeText(thisActivity, moreInfo, Toast.LENGTH_SHORT).show();

            }
        });


        //clearSkiesAdapter.setUseEventLayout(mUseEventLayout);
        //clearSkiesAdapter.notifyDataSetChanged();

        return rootView;
    }

    // had the error Binary XML file line #1: Error inflating class fragment
    // soln (I hope) at http://stackoverflow.com/questions/6424853/error-inflating-class-fragment
    // had to call getActivity inside the onActivityCreated method to ensure the right activity was being accessed
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        thisActivity = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void setAlarm(Context context) {

        Log.e(LOG_TAG, "Inside the setAlarm()");



        // Define a time value of 5 seconds
        // THIS WORKS: Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;
        // Long intervalTime = Long.valueOf(5000);
        Long alertTime = MainActivity.cal.getTimeInMillis();

        Date date = new Date(alertTime);
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String dateFormatted = formatter.format(date);

        Log.e(LOG_TAG, "Alarm Time: " + dateFormatted);


        // Define our intention of executing the ClearSkiesService class
        Intent alertIntent = new Intent(context, ClearSkiesService.AlarmReceiver.class);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in the background or isn't active
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active (don't start a new one)
        /* WORKS:
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        // Starts the alarm at the time request, and repeats it every minute
        // GOOD FOR TESTING - EXAMPLE USER TURNING THE ALARM OFF
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime,
                1000 * 60, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        */

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        // PREFERRED BY ANDROID - SEE ANDROID DOCUMENATION FOR RATIONALE
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alertTime,
                AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void updateClearSkies(Context context) {
        Intent newIntent = new Intent(context, MainActivity.class);
        startActivity(newIntent);

    }

    public void setUseEventLayout(boolean useTodayLayout) {
        mUseEventLayout = useTodayLayout;
        if (clearSkiesAdapter != null) {
            clearSkiesAdapter.setUseEventLayout(mUseEventLayout);
        }
    }

}
