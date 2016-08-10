package com.example.newglasses.clearskiesam;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by newglasses on 04/08/2016.
 */
public class ClearSkiesAdapter extends CursorAdapter {

    private final String LOG_TAG = ClearSkiesAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_COUNT = 2;

    private static final int VIEW_TYPE_EVENT = 0;
    private static final int VIEW_TYPE_EXTRA = 1;


    // Flag to determine if we want to use a separate view for "events".
    private boolean mUseEventLayout = true;

    /**
     * Cache of the children views for a list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView info1View;
        public final TextView info2View;
        public final TextView info3View;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_icon);
            info1View = (TextView) view.findViewById(R.id.list_first);
            info2View = (TextView) view.findViewById(R.id.list_second);
            info3View = (TextView) view.findViewById(R.id.list_third);
        }
    }

    public ClearSkiesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Log.e(LOG_TAG, "Inside newView()");

        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        Log.e(LOG_TAG, "View Type: " + String.valueOf(viewType));

        switch (viewType) {
            case VIEW_TYPE_EXTRA: {
                layoutId = R.layout.list_item_extra;
                break;
            }
            case VIEW_TYPE_EVENT: {
                layoutId = R.layout.list_item_event;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    // Bind the data from the cursor to the individual views
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Log.e(LOG_TAG, "Inside bindView()");

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_EXTRA: {
                // Get icon
                viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);
                /*
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                        */
                break;
            }
            case VIEW_TYPE_EVENT: {
                // Get icon
                viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);
                /*
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                        */
                break;
            }
        }

        // Read something from the cursor
        String infoOne = cursor.getString(2);
        viewHolder.info1View.setText(infoOne);

        Log.e(LOG_TAG, "infoOne:" + infoOne);

        String infoTwo = cursor.getString(3);
        //TextView middleLow = (TextView) view.findViewById(R.id.list_extra_middle_low);
        viewHolder.info2View.setText(infoTwo);
        Log.e(LOG_TAG, "infoTwo:" + infoTwo);

        String infoThree = cursor.getString(4);
        // TextView rightHigh = (TextView) view.findViewById(R.id.list_extra_right_high);
        viewHolder.info3View.setText(infoThree);
        Log.e(LOG_TAG, "infoThree:" + infoThree);

    }

    public void setUseEventLayout(boolean useEventLayout) {
        mUseEventLayout = useEventLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 1 && mUseEventLayout) ? VIEW_TYPE_EVENT : VIEW_TYPE_EXTRA;

        // When the position in the list is zero we return the event view type
        // Otherwise we return the extra view type
        //return (position == 0) ? VIEW_TYPE_EVENT : VIEW_TYPE_EXTRA;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

}
