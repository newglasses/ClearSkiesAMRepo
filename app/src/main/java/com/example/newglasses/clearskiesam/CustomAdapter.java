package com.example.newglasses.clearskiesam;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by newglasses on 11/08/2016.
 */
public class CustomAdapter extends BaseAdapter {

    private static final String LOG_TAG = CustomAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_LIST_EVENT = 0;
    private static final int VIEW_TYPE_LIST_OTHER = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseListLayout = true;

    ArrayList<String> images, textFirst, textSecond, textThird, eventStyle;
    Context context;

    private static LayoutInflater inflater = null;

    // Constructor
    public CustomAdapter(MainActivity mainActivity,
                         ArrayList<String> images,
                         ArrayList<String> textFirst,
                         ArrayList<String> textSecond,
                         ArrayList<String> textThird,
                         ArrayList<String> eventStyle) {

        this.images = images;
        this.textFirst = textFirst;
        this.textSecond = textSecond;
        this.textThird = textThird;
        this.eventStyle = eventStyle;

        context = mainActivity;
        String date = "";

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return textFirst.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    /**
     * Cache of the children views for a list item.
     */
    public class ViewHolder {
        ImageView imageView;
        TextView firstText;
        TextView secondText;
        TextView thirdText;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.list_icon);
            firstText = (TextView) view.findViewById(R.id.list_first);
            secondText = (TextView) view.findViewById(R.id.list_second);
            thirdText = (TextView) view.findViewById(R.id.list_third);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        int viewType = getItemViewType(eventStyle.get(position));
        Log.e(LOG_TAG, "viewType:" + viewType);

        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_LIST_EVENT: {
                // Get weather icon
                //holder.imageView.setImageResource(R.mipmap.ic_launcher);
                layoutId = R.layout.list_item_event;

                /*
                viewHolder.imageView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                */
                break;
            }
            case VIEW_TYPE_LIST_OTHER: {
                // Get weather icon
                //holder.imageView.setImageResource(R.mipmap.ic_launcher);
                layoutId = R.layout.list_item;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //int viewType = getItemViewType(cursor.getInt(1));
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

        holder.firstText.setText(textFirst.get(position));
        holder.secondText.setText(textSecond.get(position));
        holder.thirdText.setText(textThird.get(position));


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Clicked item is : " + textFirst.get(position), Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    public void setUseListLayout(boolean useListLayout) {
        mUseListLayout = useListLayout;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public int getItemViewType(String listType) {

        Log.e(LOG_TAG, "List Type: " + listType);
        //return (position == 0 && mUseListLayout) ? VIEW_TYPE_LIST_EVENT : VIEW_TYPE_LIST_OTHER;
        return ((listType.equals("0")) && mUseListLayout) ? VIEW_TYPE_LIST_EVENT : VIEW_TYPE_LIST_OTHER;

    }
}
