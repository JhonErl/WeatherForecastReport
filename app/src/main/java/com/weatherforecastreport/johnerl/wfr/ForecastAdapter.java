package com.weatherforecastreport.johnerl.wfr;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JohnErl on 15/11/2016.
 *
 * This is the Adapter class that create the list view entries.
 *
 */

public class ForecastAdapter  extends ArrayAdapter<JSONObject> {

    JSONArray forcast;
    ImageLoader imageLoader;
    String iconURL = "http://openweathermap.org/img/w/";

    public ForecastAdapter(Context context, int resource, JSONArray jsonArray) {
        super(context, resource);
        forcast = jsonArray;
    }

    @Override
    public int getCount() {
        return forcast.length();
    }

    /**
     * @Override
     * public View getView(int position, View view, ViewGroup parent)
     * Description: Initialize the view of the listView
     * @param position - The selected item position.
     * @param view - The view.
     * @param parent - Not in use.
     * @return view - The view.
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_wf, null);
        }
        try {
            /**
             * Initializing the listView entry.
             */
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView daytemp = (TextView) view.findViewById(R.id.daytemp);
            TextView nighttemp = (TextView) view.findViewById(R.id.nighttemp);
            TextView weather = (TextView) view.findViewById(R.id.weather);
            ImageView icon = (ImageView) view.findViewById(R.id.icon);

            imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));

            // Loading the data to the views.
            JSONObject mainobj = forcast.getJSONObject(position);
            date.setText(new SimpleDateFormat("EEEE").format(new Date(mainobj.getLong("dt")*1000l)));
            JSONObject weatherobj = mainobj.getJSONArray("weather").getJSONObject(0);
            JSONObject temp = mainobj.getJSONObject("temp");
            weather.setText(weatherobj.getString("main"));
            daytemp.setText(temp.getString("day" )+ '\u00b0');
            nighttemp.setText(temp.getString("night")+ '\u00b0');
            imageLoader.displayImage(iconURL + weatherobj.getString("icon") + ".png", icon);
            Log.i("JSON object", mainobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
}
