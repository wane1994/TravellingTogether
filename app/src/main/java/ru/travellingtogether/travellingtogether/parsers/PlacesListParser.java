package ru.travellingtogether.travellingtogether.parsers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleAdapter;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/** A class to parse the Google Places in JSON format */
public class PlacesListParser extends AsyncTask<String, Integer, List<HashMap<String,String>>> {
    AutoCompleteTextView source;
    Context context;
    JSONObject jObject;

    public PlacesListParser(AutoCompleteTextView acTV, Context ctx) {
        source = acTV;
        context = ctx;
    }

    @Override
    protected List<HashMap<String, String>> doInBackground(String... jsonData) {
        List<HashMap<String, String>> places = null;
        PlacesJSONParser placesJsonParser = new PlacesJSONParser();

        try{
            jObject = new JSONObject(jsonData[0]);
            // Getting the parsed data as a List construct
            places = placesJsonParser.parse(jObject);
        } catch (Exception e){
            Log.d("Exception", e.toString());
        }

        return places;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> result) {
        String[] from = new String[] { "description"};
        int[] to = new int[] { android.R.id.text1 };

        // Creating a SimpleAdapter for the AutoCompleteTextView
        SimpleAdapter adapter = new SimpleAdapter(context, result, android.R.layout.simple_list_item_1, from, to);
        // Setting the adapter
        source.setAdapter(adapter);
    }
}