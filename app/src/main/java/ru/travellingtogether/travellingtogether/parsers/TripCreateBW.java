package ru.travellingtogether.travellingtogether.parsers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

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

import ru.travellingtogether.travellingtogether.fragments.FragmentMap;

// sending JSON request to server and getting answer
// Create a trip: send new trip data to database
public class TripCreateBW extends AsyncTask<String,Void,String> {
    Context context;
    ProgressDialog loading;
    public FragmentMap source = null;

    public TripCreateBW(FragmentMap fm, Context ctx) {
        source = fm;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String createTrip_url = "https://travelling-together.000webhostapp.com/php/createtrip.php";

        try {
            String userstatus = params[0];
            String username = params[1];
            String from = params[2];
            String to = params[3];
            String day = params[4];
            String month = params[5];
            String year = params[6];
            String hour = params[7];
            String minute = params[8];
            URL url = new URL(createTrip_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to insert data in database
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode(userstatus,"UTF-8")+"&"
                    +URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                    +URLEncoder.encode("from","UTF-8")+"="+URLEncoder.encode(from,"UTF-8")+"&"
                    +URLEncoder.encode("to","UTF-8")+"="+URLEncoder.encode(to,"UTF-8")+"&"
                    +URLEncoder.encode("day","UTF-8")+"="+URLEncoder.encode(day,"UTF-8")+"&"
                    +URLEncoder.encode("month","UTF-8")+"="+URLEncoder.encode(month,"UTF-8")+"&"
                    +URLEncoder.encode("year","UTF-8")+"="+URLEncoder.encode(year,"UTF-8")+"&"
                    +URLEncoder.encode("hour","UTF-8")+"="+URLEncoder.encode(hour,"UTF-8")+"&"
                    +URLEncoder.encode("minute","UTF-8")+"="+URLEncoder.encode(minute,"UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            // reading answer from server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result="";
            String line="";
            while ((line = bufferedReader.readLine())!=null) {
                result = line;
            }
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();
            return result;

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
    protected void onPostExecute(String result) {
        loading.dismiss();
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        if (result.equals("Your trip created successfully")) {
            source.tripcreateBWPE();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}