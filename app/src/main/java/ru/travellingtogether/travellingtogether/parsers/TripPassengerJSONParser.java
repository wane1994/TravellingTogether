package ru.travellingtogether.travellingtogether.parsers;

import android.app.ProgressDialog;
import android.content.Context;
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

import ru.travellingtogether.travellingtogether.fragments.FragmentDriver;

// sending JSON request to server and getting JsonString answer
// Trip list: get list of trips created by passengers
public class TripPassengerJSONParser extends AsyncTask<String,Void,String> {
    public static String jsonResult = null;
    public FragmentDriver source = null;
    Context context;
    ProgressDialog loading;
    public TripPassengerJSONParser(FragmentDriver fp, Context ctx) {
        source = fp;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String gettriplist_url = "http://travellingtogether.ru/gettriplist.php";

        try {
            String userstatus = params[0];
            URL url = new URL(gettriplist_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to get trip list
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(userstatus, "UTF-8");
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
        loading = ProgressDialog.show(context, "Please wait...",null,true,true);
    }

    @Override
    protected void onPostExecute(String jsonResult) {
        loading.dismiss();
        source.passengerJSONPE(jsonResult);
    }
}