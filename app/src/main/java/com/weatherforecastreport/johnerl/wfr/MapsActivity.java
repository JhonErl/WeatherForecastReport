package com.weatherforecastreport.johnerl.wfr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatConversionException;
/**
 *   This is the main Activity here I load the map and all the additional attributes of the map.
 *
 *   the functionalist's of this activity are:
 *   - creation of markers at any place on the map that a city name exists via long click.
 *   - saving/removing the markers on to a single Shared Preferences file.
 *   - on a sing click on a marker we get the city name from the InfoWindow and an AlertDialog
 *     that responsible for the deletion of the marker and transition to the next activity.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    /** SharedPreferences used to save the markers even after the onDestroy() is called when closing the app*/
    private SharedPreferences sharedPref;
    /** A tag for log messages*/
    private static final String TAG = "MapsActivity address";
    /** This is the main class of the Google Maps Android API and is the entry point for all methods related to the map*/
    private GoogleMap mMap;
    /** The name of the last marker the user clicked on*/
    private String locName;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sharedPref = getPreferences(MODE_PRIVATE); //In order to open one shared preference file.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        try {
            //moves through the shared Preferences and adds the saved markers
            addMarkerFromSHP();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("PROBLEM", e.getMessage());
        }

        /**
         * public final void setOnMarkerClickListener (GoogleMap.OnMarkerClickListener listener)
         * Sets a callback that's invoked when a marker is clicked.
         *
         * public static interface GoogleMap.OnMarkerClickListener
         * Defines signatures for methods that are called when a marker is clicked or tapped.
         */
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            /**
             * public abstract boolean onMarkerClick (Marker marker)
             * Called when a marker has been clicked or tapped.
             *
             * Description: opens the info window and build, create and  launch the
             *              alert dialog.
             *
             * @param marker
             * @return
             */
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.showInfoWindow();
                locName = marker.getTitle(); // Setting the city name to the current selected city.
                    AlertDialog.Builder alertDialogBuilder;
                    try {
                    alertDialogBuilder = getAlertDialogBuilder(marker);
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i("BIG Problem", e.getMessage());
                }
                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            /**
             * @Override
             * public void onInfoWindowClick(Marker marker)
             * Description: calling passToWFActivity()
             * @param marker
             */
            @Override
            public void onInfoWindowClick(Marker marker) {
                locName = marker.getTitle(); // Setting the city name to the current selected city.
                passToWFActivity();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            /**
             * @Override
             * public void onMapLongClick(LatLng latLng)
             * Description: Creating the marker on the clicked location.
             * @param latLng
             */
            @Override
            public void onMapLongClick(LatLng latLng) {
                try {
                    List<Address> address = getAddress(latLng.latitude, latLng.longitude);
                    locName = address.get(0).getLocality();
                    if(locName == null) throw new Exception();
                    Log.i("city name", " "+locName);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(locName));
                    saveToSharedPref(latLng, locName);
                } catch (UnknownFormatConversionException e) {
                    e.printStackTrace();
                    Log.i("EXCEPTION:", e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MapsActivity.this, "location unavailable", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    /**
     * private void passToWFActivity()
     * Description: Sending to the weather activity.
     */
    private void passToWFActivity() {
        Intent intent = new Intent(MapsActivity.this, WFActivity.class);
        Log.i("locName", " "+ locName);
        intent.putExtra("name", locName);
        startActivityForResult(intent, 1);
    }

    /**
     * void addMarkerFromSHP() throws Exception
     *
     * Description: The function shall retrieve all the data from the shard preferences file
     *              and parse it to the map marker.
     *
     * @throws Exception
     */
    private void addMarkerFromSHP() throws Exception {
        sharedPref = getPreferences(MODE_PRIVATE);
        Map<String, ?> map = sharedPref.getAll();
        /**
         * The data is stored ih th the file as a string that look like this:
         * "latitude,longitude" making it easy to store, retrieve and pare the data.
         * After retrieving, splitting and parsing the latitude and longitude of each marker,
         * I create a LatLng object in order to retrieve the address that contains the city name and
         * to create a marker and add it to the map.
         */
        for (String key : map.keySet())
        {
            String coord = (String) map.get(key);
            String[] coordinates = coord.split(",");
            Double lat = Double.parseDouble(coordinates[0]);
            Double lon = Double.parseDouble(coordinates[1]);
            Log.i("LOCATIN:", "lat: " + lat.toString() + " lon: " + lon.toString());
            LatLng latLng = new LatLng(lat, lon);
            List<Address> address = getAddress(latLng.latitude, latLng.longitude);
            locName = address.get(0).getLocality();
            mMap.addMarker(new MarkerOptions().position(latLng).title(locName));
        }
    }

    /**
     * @NonNull
     * private AlertDialog.Builder getAlertDialogBuilder(final Marker marker) throws Exception
     *
     * Description: this funcfion creates the dialog builder,
     *              sets the textual descriptions presented on the dialog window,
     *              and sets the buttons functionality.
     *
     * @param marker
     * @return AlertDialog.Builder - The dialog instructions.
     * @throws Exception
     */
    @NonNull
    private AlertDialog.Builder getAlertDialogBuilder(final Marker marker) throws Exception {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        alertDialogBuilder.setTitle(R.string.dialog_text);
        alertDialogBuilder.setMessage("Clike yes to remove marker").setCancelable(false)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    /**
                     * @Override
                     * public void onClick(DialogInterface dialog, int which)
                     * Description: Setting the yes button which  removes the selected marker.
                     * @param dialog - Not used.
                     * @param which - Not used.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.remove(marker.getTitle());
                        editor.commit();
                        marker.remove();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            /**
             * @Override
             * public void onClick(DialogInterface dialog, int id)
             * Description: Setting the yes button which send the user to weather activity
             * @param dialog - Not used.
             * @param id - Not used.
             */
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

                passToWFActivity();
            }
        });
        return alertDialogBuilder;
    }

    /**
     * private void saveToSharedPref(LatLng latLng, String name)
     * Description: opens the shared preference file to edit,
     *              parse the data to a string and store it in the file
     *              as key value pair where the city name is the key and
     *              the coordinate are the value.
     *
     * @param latLng the geo location of the city.
     * @param name  the name of the city.
     */
    private void saveToSharedPref(LatLng latLng, String name) {
        SharedPreferences.Editor editor = sharedPref.edit();
        String value = String.format("%f,%f", latLng.latitude, latLng.longitude);
        Log.i("VALUE", value);
        editor.putString(name, value);
        editor.commit();
    }

    /**
     * public List<Address> getAddress(Double latitude, Double longitude)
     * Description: Here I used the geo coordinate to fetch the address that contain
     *              the cities names using a Geocoder object.
     *
     * @param latitude
     * @param longitude
     * @return List<Address> - Containing the city name.
     */
    public List<Address> getAddress(Double latitude, Double longitude) {
        String errorMessage = "";
        List<Address> address = null;
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        try {

            address = geocoder.getFromLocation(latitude, longitude, 1);

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.i(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.i(TAG, errorMessage + ". " +
                    "Latitude = " + latitude +
                    ", Longitude = " +
                    longitude, illegalArgumentException);
        }
        return address;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
