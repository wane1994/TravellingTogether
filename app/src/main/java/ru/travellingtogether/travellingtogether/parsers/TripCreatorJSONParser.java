package ru.travellingtogether.travellingtogether.parsers;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import ru.travellingtogether.travellingtogether.fragments.FragmentTripList;

// sending JSON request to server and getting JsonString answer
// Trip creator info: get data about user that created chosen trip
public class TripCreatorJSONParser extends AsyncTask<String,Void,String>{
    public static String jsonResult=null;
    public FragmentTripList source = null;
    public TripCreatorJSONParser(FragmentTripList ftl) {
        source = ftl;
    }

    @Override
    protected String doInBackground(String... params) {
        String getcreatordata_url = "http://travellingtogether.ru/getcreatordata.php";
        try {
            String username = params[0];
            URL url = new URL(getcreatordata_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to get user data
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            // reading answer from server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            jsonResult = bufferedReader.readLine();
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();
            return jsonResult;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String jsonResult) {
        source.creatorJSONPE();
    }

}
