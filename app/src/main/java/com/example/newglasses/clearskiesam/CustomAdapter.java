package com.example.newglasses.clearskiesam;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by newglasses on 11/08/2016.
 * CustomAdapter that organises the ListViews that populate the UI
 * Code developed from two sources
 * Udacity course: https://classroom.udacity.com/courses/ud853/lessons/1623168625/concepts/16677585740923#
 * ListView example: http://www.androidtutorialsworld.com/android-custom-listview-example/
 *
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
                         ArrayList<String> textFirst,
                         ArrayList<String> textSecond,
                         ArrayList<String> textThird,
                         ArrayList<String> eventStyle) {

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
                layoutId = R.layout.list_item_event;
                break;
            }
            case VIEW_TYPE_LIST_OTHER: {
                layoutId = R.layout.list_item;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        switch (viewType) {
            case VIEW_TYPE_LIST_EVENT: {
                break;
            }
            case VIEW_TYPE_LIST_OTHER: {
                viewHolder.imageView.setImageResource(R.drawable.cs_smaller);
                break;
            }
        }

        holder.firstText.setText(textFirst.get(position));
        holder.secondText.setText(textSecond.get(position));
        holder.thirdText.setText(textThird.get(position));


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "Clicked item is : " + textFirst.get(position), Toast.LENGTH_LONG).show();
                // TODO: When clicked the ListItem opens weather or event details
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
        return ((listType.equals("0")) && mUseListLayout) ? VIEW_TYPE_LIST_EVENT : VIEW_TYPE_LIST_OTHER;

    }
}
