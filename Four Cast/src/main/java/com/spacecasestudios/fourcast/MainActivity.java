package com.spacecasestudios.fourcast;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;


public class MainActivity extends ActionBarActivity {

   protected TextView mCityLabel;
   protected TextView mHumidLabel;
   protected TextView mWindLabel;
   protected TextView mPrecipLabel;
   protected TextView mTempLabel;
   protected ImageView mWeatherImage;

   protected String mWeather;
   protected String mWindSpeed;
   protected String mPrecipitation;
   protected String mTemperature;
   protected JSONObject mWeatherData;
   protected String mLocality;
   private float mLat, mLng;
   public static final String TAG = MainActivity.class.getSimpleName();
   private LocationManager locationManager;
   private String provider;
   final String DEGREE  = "\u00b0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCityLabel = (TextView) findViewById(R.id.textViewCity);
        mTempLabel = (TextView) findViewById(R.id.textViewTemp);
        mHumidLabel = (TextView) findViewById(R.id.textViewHumidity);
        mWindLabel = (TextView) findViewById(R.id.textViewWind);
        mPrecipLabel = (TextView) findViewById(R.id.textViewPrecipitation);
        mWeatherImage = (ImageView) findViewById(R.id.imageViewWeatherIcon);



        setLatAndLng();
        setLocality();

        //Get the weather data in an Async Task by connecting to the forecast api
        if(isNetworkAvailable()){
            GetWeatherData getWeatherData = new GetWeatherData();
            getWeatherData.execute();
        }
        else{
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }

    }

    private void setLocality() {
        //Retrieve the current Locality (City) / hard code if location is unavailable
        Geocoder gcd = new Geocoder(this);
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(mLat, mLng, 1);
            mLocality = addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea();
            Log.i(TAG, "The City is: " + mLocality);
        } catch (Exception e) {
            Log.e(TAG, "Exception caught:", e);
            mLocality = "Somewhere out there";
            Log.i(TAG, "The City is: " + mLocality);
        }
    }

    private void setLatAndLng() {
        // Location manager retrieves the current Latitude and Longitude
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        //if the location is available set the mLat and mLng otherwise hard code the values
        if (location != null) {
            mLat =  (float)(location.getLatitude());
            mLng = (float)(location.getLongitude());
            Log.i(TAG, "The mLat: " + mLat + " and the mLng is: " + mLng);
        }
        else{
            Toast.makeText(this, "Current Location is unavailable", Toast.LENGTH_LONG).show();
            //Ulaanbaatar Mongolia
            mLat = 47.9200f;
            mLng = 106.9200f;
            Log.i(TAG, "The mLat: " + mLat + " and the ln is: " + mLng);
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

    private void updateData() {
        if(mWeatherData == null){
            /* TODO: Handle Error */
            Log.i("In Update Data", "We have some null data ");
        }
        else{
            try {
                JSONObject jsonCurrentWeather = mWeatherData.getJSONObject("currently");
                mWeather = jsonCurrentWeather.getString("icon");

                //Format Strings for display
                mTemperature = Integer.toString(jsonCurrentWeather.getInt("temperature")) + " " + DEGREE + "F";
                mWindSpeed =  Integer.toString(jsonCurrentWeather.getInt("windSpeed")) + "mph";
                mPrecipitation = Float.toString(jsonCurrentWeather.getInt("precipProbability")) + "%";

                mHumidLabel.setText(Float.toString(jsonCurrentWeather.getInt("humidity")));
                mPrecipLabel.setText(mPrecipitation);
                mWindLabel.setText(mWindSpeed);
                mTempLabel.setText(mTemperature);
                mCityLabel.setText(mLocality);

                Log.i("In Update Data", "The weather is: " + mWeather +
                        " and the temperature is " + Integer.toString(jsonCurrentWeather.getInt("temperature")));

                if(mWeather.equals("clear-day")){
                   mWeatherImage.setImageResource(R.drawable.sunny);
                }
                else if(mWeather.equals("rain")){
                    mWeatherImage.setImageResource(R.drawable.rain);
                }
                else if(mWeather.equals("clear-night")){
                    mWeatherImage.setImageResource(R.drawable.clear_night);
                }
                else if(mWeather.equals("snow")){
                    mWeatherImage.setImageResource(R.drawable.snow);
                }
                else if(mWeather.equals("partly-cloudy-day")){
                    mWeatherImage.setImageResource(R.drawable.partyly_cloudy);
                }
                else if(mWeather.equals("partly-cloudy-night")){
                    mWeatherImage.setImageResource(R.drawable.cloudy_night);
                }
                else{
                    //default image in case of unknown weather condition
                    mWeatherImage.setImageResource(R.drawable.earth);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception caught!", e);
            }
        }
    }

    //clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night

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

    private class GetWeatherData extends AsyncTask<Object, Void, JSONObject> {

        protected JSONObject doInBackground(Object... params) {
            int responseCode = -1;
            String url = "https://api.forecast.io/forecast/48ab6b9045bb8c5558c38a43d0e337c0/" + Float.toString(mLat)+ "," + Float.toString(mLng);
            JSONObject jsonResponse = null;
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);

            try {
                HttpResponse response = client.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                responseCode = statusLine.getStatusCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line = reader.readLine()) != null){
                        builder.append(line);
                    }

                    jsonResponse = new JSONObject(builder.toString());
                }
                else {
                    Log.i(TAG, String.format("Unsuccessful HTTP response code: %d", responseCode));
                }
            }
            catch (JSONException e) {
                Log.e(TAG, "Exception caught:", e);
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught:", e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            mWeatherData = result;
            updateData();
        }
    }

}

