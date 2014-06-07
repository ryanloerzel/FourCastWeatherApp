package com.spacecasestudios.fourcast;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

   private TextView mWeatherLabel;
   public static final int NUMBER_OF_LOCATIONS = 4;
   public static final String TAG = MainActivity.class.getSimpleName();
   private LocationManager locationManager;
   private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeatherLabel = (TextView) findViewById(R.id.textView3);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            int lat = (int) (location.getLatitude());
            int lng = (int) (location.getLongitude());
            System.out.println("Latitude: " + lat + " Longitude: " + lng);
        }
        else{
            System.out.println("Location not provided");
        }

        if(isNetworkAvailable()){
            GetWeatherData getWeatherData = new GetWeatherData();
            getWeatherData.execute();
        }
        else{
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.spacecasestudios.fourcast.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == com.spacecasestudios.fourcast.R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetWeatherData extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            int responseCode = -1;

            //Attempt a connection with the weather data URL
            try {
                URL weatherFeedUrl = new URL("https://api.forecast.io/forecast/48ab6b9045bb8c5558c38a43d0e337c0/40.5833,-122.3667");
                HttpURLConnection connection = (HttpURLConnection) weatherFeedUrl.openConnection();
                connection.connect();
                responseCode = connection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);

                    //ERROR WHEN REQUESTING CONTENT LENGTH: -1 RETURNED*********************
//                    int contentLength = connection.getContentLength();
//                    if(contentLength == -1) {
//                        //contentLength = connection.getHeaderFieldInt("Length", -1);
//                        contentLength = 2000;
//                    }
//                    Log.v(TAG, Integer.toString(contentLength));
//                    char[] charArray = new char[contentLength];
//                    reader.read(charArray);
//                    String responseData = new String(charArray);
                    //************************************************************************

                    //THIS fix was written by Hubert Maraszek*********************************
                    int nextCharacter; // read() returns an int, we cast it to char later
                    String responseData = "";
                    while(true){ // Infinite loop, can only be stopped by a "break" statement
                        nextCharacter = reader.read(); // read() without parameters returns one character
                        if(nextCharacter == -1) // A return value of -1 means that we reached the end
                            break;
                        responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                    }
                    //************************************************************************
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject jsonCurrentWeather = jsonResponse.getJSONObject("currently");
                    String currentWeather = jsonCurrentWeather.getString("summary");
                    mWeatherLabel.setText(currentWeather);
                    //Log.i(TAG, currentWeather);

                }
                else {
                    Log.i(TAG, "Unsuccessful Http Response Code: " + responseCode);
                }

            }
            catch (MalformedURLException e) {
                Log.e(TAG, "Exception caught:", e);
            }
            catch (IOException e){
                Log.e(TAG, "Exception caught:", e);
            }
            catch (Exception e){
                Log.e(TAG, "Exception caught:", e);
            }

            return "Code :" + responseCode;
        }
    }


}
