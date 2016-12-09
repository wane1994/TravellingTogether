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

import ru.travellingtogether.travellingtogether.fragments.FragmentLogin;

// sending JSON request to server and getting JsonString answer
// Login stage: get user data for username:password
public class LoginJSONParser extends AsyncTask<String,Void,String> {
    public static String jsonResult=null;
    public FragmentLogin source = null;
    public LoginJSONParser(FragmentLogin fl) {
        source = fl;
    }

    @Override
    protected String doInBackground(String... params) {
        String getdata_url = "http://192.168.137.1/getuserdata.php";

        try {
            String username = params[0];
            String password = params[1];
            URL url = new URL(getdata_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to get user data
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&"
                    + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
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
        source.loginJSONPE(jsonResult);
    }
}