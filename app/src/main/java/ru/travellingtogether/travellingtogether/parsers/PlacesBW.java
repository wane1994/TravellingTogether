package ru.travellingtogether.travellingtogether.parsers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/** sending JSON request to server and getting JsonString answer */
public class PlacesBW extends AsyncTask<String, Void, String> {
    AutoCompleteTextView source;
    Context context;
    PlacesListParser placesListParser = new PlacesListParser(source, context);

    public PlacesBW(AutoCompleteTextView acTV, Context ctx) {
        source = acTV;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... place) {
        // For storing data from web service
        String data = "";
        // Google API browser key
        String key = "key=AIzaSyDI-nnfsI6rOIwgVFTIA3_Um9pPGEsN8Qw";

        String input="";

        try {
            input = "input=" + URLEncoder.encode(place[0], "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // type to be searched
        String types = "types=geocode";
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = input+"&"+types+"&"+sensor+"&"+key;
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        try{
            // Fetching the data from web service
            data = downloadUrl(url);
        } catch (Exception e){
            Log.d("Background Task", e.toString());
        }

        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // Creating PlacesListParser
        placesListParser = new PlacesListParser(source, context);
        // Starting Parsing the JSON string returned by Web Service
        placesListParser.execute(result);
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            httpURLConnection.connect();
            // Reading data from url
            inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            bufferedReader.close();

        } catch (Exception e){
            Log.d("Exception when dwnl url", e.toString());
        } finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }

        return data;
    }
}