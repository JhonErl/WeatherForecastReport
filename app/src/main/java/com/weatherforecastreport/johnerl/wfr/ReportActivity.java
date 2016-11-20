package com.weatherforecastreport.johnerl.wfr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  This Activity gives more details on a specific day, and changes background image
 *  according to the sky condition.
 */

public class ReportActivity extends AppCompatActivity {

    private JSONObject weather;                                         // The Json object holding the data.
    private ImageLoader imageLoader;                                    // The object maneging the image.
    private String iconURL = "http://openweathermap.org/img/w/";        // the server image URL.
    private JSONObject temp;
    private JSONObject dailyWeather;
    private ImageView icon;
    private RelativeLayout relativeLayout;
    private Boolean changeThemeFlage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        /**
         * Initializing all the views in the activity with the proper values.
         */
        icon = (ImageView) findViewById(R.id.iconDaily2);
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_report);
        try {
            dailyWeather = new JSONObject(getIntent().getStringExtra("json"));
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(ReportActivity.this));

            ((TextView) findViewById(R.id.dateToday2)).setText(new SimpleDateFormat("EEEE").format(new Date(dailyWeather.getLong("dt") * 1000l)));
            weather = dailyWeather.getJSONArray("weather").getJSONObject(0);
            ((TextView) findViewById(R.id.description2)).setText("Sky condition: " + weather.getString("description"));
            imageLoader.displayImage(iconURL + weather.getString("icon") + ".png", icon);

            temp = dailyWeather.getJSONObject("temp");
            ((TextView) findViewById(R.id.day2)).setText("Day temperature"+temp.getString("day")+ '\u00b0');
            ((TextView) findViewById(R.id.min2)).setText("Minimum "+temp.getString("min")+ '\u00b0');
            ((TextView) findViewById(R.id.max2)).setText("Maximum temperature"+temp.getString("max")+ '\u00b0');
            ((TextView) findViewById(R.id.night2)).setText("Temperature at night "+ temp.getString("night")+ '\u00b0');
            ((TextView) findViewById(R.id.eve2)).setText("Temperature at the evening "+ temp.getString("eve")+ '\u00b0');
            ((TextView) findViewById(R.id.morn2)).setText("Temperature at the morning "+ temp.getString("morn")+ '\u00b0');

            ((TextView) findViewById(R.id.pressure2)).setText("atmospheric pressure is "+ dailyWeather .getString("pressure")+"Bar");
            ((TextView) findViewById(R.id.humit2)).setText("Humidity is "+ dailyWeather .getString("humidity")+"%");
            ((TextView) findViewById(R.id.speed2)).setText("Wind speed is "+dailyWeather .getString("speed")+"mph");
            ((TextView) findViewById(R.id.deg2)).setText(dailyWeather .getString("deg")+ '\u00b0');
            ((TextView) findViewById(R.id.clouds2)).setText("Visibility is "+ dailyWeather .getString("clouds")+"%");
            /**
            * Choosing a background image according to the sky condition.
            */
            switch (weather.getString("main")){
                case "Clear":
                    relativeLayout.setBackgroundResource(R.drawable.clearsky);
                    break;
                case "Clouds":
                    relativeLayout.setBackgroundResource(R.drawable.fewclouds);
                    break;
                case "Rain":
                    relativeLayout.setBackgroundResource(R.drawable.rain);
                    this.setTheme(R.style.whaitecolorstyle);
                    changeThemeFlage = true;
                    break;
                case "Snow":
                    relativeLayout.setBackgroundResource(R.drawable.snow);
                    break;
                case "Mist":
                    relativeLayout.setBackgroundResource(R.drawable.mist);
                    break;
                default:
                    relativeLayout.setBackgroundResource(R.color.wallet_dim_foreground_disabled_holo_dark);
            }
        if(changeThemeFlage){
//            this.setTheme(R.style.MyTextViewBlackStyle);
            changeThemeFlage =false;
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
