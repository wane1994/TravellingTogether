package ru.travellingtogether.travellingtogether.parsers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import ru.travellingtogether.travellingtogether.fragments.FragmentMap;

// sending JSON request to server and getting JsonString answer
// Geocode: get From and To points from fragment and receive result with Google Maps Geocoding API
public class MyGeocoder extends AsyncTask<String,Void,String> {
    public static String jsonResult = null;
    public FragmentMap source = null;
    Context context;
    ProgressDialog loading;
    public MyGeocoder(FragmentMap fm, Context ctx) {
        source = fm;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            String link = "http://maps.google.com/maps/api/geocode/json?address=";
            String sensor = "&sensor=true";
            String language = "&language=en";
            String address = params[0];
            // Building the url to the web service
            String getdata_url = link + URLEncoder.encode(address, "UTF-8") + sensor + language;
            URL url = new URL(getdata_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);

            // reading answer from server
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line="";
            while ((line = bufferedReader.readLine())!=null) {
                sb.append(line + "\n");
            }
            jsonResult = sb.toString();
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    @Override
    protected void onPreExecute() {
        loading = ProgressDialog.show(context, "Please wait...",null,true,true);
    }

    @Override
    protected void onPostExecute(String jsonResult) {
        loading.dismiss();
        source.myGeocoderPE(jsonResult);
    }
}