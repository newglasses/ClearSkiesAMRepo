package com.example.newglasses.clearskiesam;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by newglasses on 10/08/2016.
 * Developed with help from: http://www.androidtutorialsworld.com/android-custom-listview-example/
 * & understanding the MatrixCursor: XXX
 */
public class CustomListViewAdapter extends CursorAdapter {

    private static final String LOG_TAG = CustomListViewAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_LIST_EVENT = 0;
    private static final int VIEW_TYPE_LIST_OTHER = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseListLayout = true;

    int [] images;    String [] names;
    Context context;
    private static LayoutInflater inflater = null;

    public CustomListViewAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ViewHolder {
        public final ImageView imageView;
        public final TextView firstText;
        public final TextView secondText;
        public final TextView thirdText;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.list_icon);
            firstText = (TextView) view.findViewById(R.id.list_first);
            secondText = (TextView) view.findViewById(R.id.list_second);
            thirdText = (TextView) view.findViewById(R.id.list_third);
        }
    }

    /*
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder=new ViewHolder();

        View rootView = inflater.inflate(R.layout.list_item , null);
        holder.tv = (TextView) rootView.findViewById(R.id.myTV);
        holder.iv=(ImageView) rootView.findViewById(R.id.myIV);
        holder.tv.setText(names[position]);
        holder.iv.setImageResource(images[position]);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Clicked item is : " + names[position], Toast.LENGTH_LONG).show();
            }
        });
        return rootView;
    }
    */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getInt(1));

        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_LIST_EVENT: {
                layoutId = R.layout.list_item_event;
                break;
            }
            case VIEW_TYPE_LIST_OTHER: {
                layoutId = R.layout.list_item;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getInt(1));
        switch (viewType) {
            case VIEW_TYPE_LIST_EVENT: {
                // Get weather icon
                viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);

                /*
                viewHolder.imageView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                */
                break;
            }
            case VIEW_TYPE_LIST_OTHER: {
                // Get weather icon
                viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
                break;
            }
        }

        /* Read date from cursor
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        */

        // Get date today in friendly format
        //long dateInMillis = 1470833833305L;
        // Read weather forecast from cursor
        long first = cursor.getLong(3);
        // Find TextView and set formatted date on it
        viewHolder.firstText.setText(Utility.getFriendlyDayString(context, first));

        // Get date tomorrow in friendly format
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long tomorrowInMillis = calendar.getTimeInMillis();
        // Find TextView and set weather forecast on it
        viewHolder.secondText.setText(Utility.getFriendlyDayString(context, tomorrowInMillis));

        /*
        // Read weather forecast from cursor
        String first = cursor.getString(3);
        // Find TextView and set weather forecast on it
        viewHolder.firstText.setText(first);
        // For accessibility, add a content description to the icon field
        //viewHolder.imageView.setContentDescription(first);

        // Read weather forecast from cursor
        String second = cursor.getString(4);
        // Find TextView and set weather forecast on it
        viewHolder.secondText.setText(second);

        */

        // Read weather forecast from cursor
        String third = cursor.getString(5);
        // Find TextView and set weather forecast on it
        viewHolder.thirdText.setText(third);

    }

    public void setUseListLayout(boolean useListLayout) {
        mUseListLayout = useListLayout;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int listType) {

        Log.e(LOG_TAG, "List Type: " + listType);
        //return (position == 0 && mUseListLayout) ? VIEW_TYPE_LIST_EVENT : VIEW_TYPE_LIST_OTHER;
        return (listType == 0 && mUseListLayout) ? VIEW_TYPE_LIST_EVENT : VIEW_TYPE_LIST_OTHER;

    }
}
