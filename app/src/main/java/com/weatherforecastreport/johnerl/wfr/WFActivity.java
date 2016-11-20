package com.weatherforecastreport.johnerl.wfr;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This is the Weather forecast activity it fetches the forecast data and display it.
 *
 * the functionalist's of this activity are:
 * - Getting the data from the openweathermap server.
 * - Creating and displaying the data to a list view.
 * - Sending the user to a daily weather report.
 */
public class WFActivity extends AppCompatActivity {

    private ListView listview;
    private String cityName;
    private JSONObject json;
    /**
     * The URL header and tailer.
     */
    private static final String beginURL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=";
    private static final String endURL = "&mode=Json&units=metric&cnt=7&appid=9f3dfd22da593d6c74c3f991e1b32790";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wf);
        cityName = getIntent().getStringExtra("name");
        listview = (ListView) findViewById(R.id.listView);
        Log.i("cityName"," "+cityName);
        this.setTitle(cityName);
        new Thread(getForcast).start();
    }

    /**
     * Creating a Runnable object in order to run the make the
     * call to the server in the background.
     */
    Runnable getForcast = new Runnable() {
        /**
         * @Override
         * public void run()
         * Description: The function shall replace all the spaces from the city name with %20
         *              in order to make it part of the URL. Then fetch the the data as a Json
         *              object from the server.
         */
        @Override
        public void run() {
            try {
                String name = cityName.replaceAll(" ", "%20");
                Log.i("name of city", name);
                json = fetchURL(beginURL + name + endURL);
                // The json array is modified as final in order to use in the runOnUiThread() function.
                final JSONArray list = json.getJSONArray("list");
                /**
                 * final void runOnUiThread(Runnable action)
                 * Description: Runs the specified action on the UI thread.
                 */
                WFActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listview.setAdapter(new ForecastAdapter(WFActivity.this, R.layout.item_wf, list));
                            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                /**
                                 * @Override
                                 * public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                 * Description: Extracts the weather report of a single day,
                                 *              and send the user to the next activity.
                                 * @param parent - Not in use.
                                 * @param view - Not in use.
                                 * @param position - The selected item position.
                                 * @param id - Not in use.
                                 */
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    try {
                                        JSONObject j = list.getJSONObject(position);
                                        Log.i("json object: ",j.toString()+ "in position: " + position );
                                        Intent intent = new Intent(WFActivity.this,ReportActivity.class);
                                        intent.putExtra("json", j.toString());
                                        startActivityForResult(intent, 2);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * public JSONObject fetchURL(String url) throws Exception
     * Description: The function create an OkHttp clint instance, then a request
     *              from the server with the url, and then get the response.
     *              And parse it to a Json object in order to return it easily.
     * @param url - The full URL.
     * @return JSONObject - containing the weather forecast for the next weak.
     * @throws Exception
     */
    public JSONObject fetchURL(String url) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return new JSONObject(response.body().string());
    }

}
