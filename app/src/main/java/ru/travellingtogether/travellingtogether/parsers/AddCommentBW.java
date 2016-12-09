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

import ru.travellingtogether.travellingtogether.fragments.FragmentTripDetails;

// sending JSON request to server and getting answer
// Add comment: send new comment data to database
public class AddCommentBW extends AsyncTask<String,Void,String> {
    Context context;
    ProgressDialog loading;
    public FragmentTripDetails source = null;

    public AddCommentBW(FragmentTripDetails ftd, Context ctx) {
        source = ftd;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String addcomment_url = "http://travellingtogether.ru/addcomment.php";

        try {
            String tripid = params[0];
            String username = params[1];
            String comment = params[2];
            URL url = new URL(addcomment_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to insert data in database
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("tripid","UTF-8")+"="+URLEncoder.encode(tripid,"UTF-8")+"&"
                    +URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                    +URLEncoder.encode("comment","UTF-8")+"="+URLEncoder.encode(comment,"UTF-8");
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
        if (result.equals(" Comment added ")) {
            source.addcommentBWPE();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}