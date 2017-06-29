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

import ru.travellingtogether.travellingtogether.fragments.FragmentUpdateInfo;

// sending JSON request to server and getting answer
// Update stage: update user data in database
public class UpdateBW extends AsyncTask<String,Void,String> {
    Context context;
    ProgressDialog loading;
    public FragmentUpdateInfo source = null;

    public UpdateBW(FragmentUpdateInfo fti, Context ctx) {
        source = fti;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String updateinfo_url = "https://travelling-together.000webhostapp.com/php/updateuserdata.php";

        try {
            String username = params[0];
            String oldpassword = params[1];
            String newpassword = params[2];
            String name = params[3];
            String surname = params[4];
            String phonenumber = params [5];
            URL url = new URL(updateinfo_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to update data in database
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                    +URLEncoder.encode("oldpassword","UTF-8")+"="+URLEncoder.encode(oldpassword,"UTF-8")+"&"
                    +URLEncoder.encode("newpassword","UTF-8")+"="+URLEncoder.encode(newpassword,"UTF-8")+"&"
                    +URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"
                    +URLEncoder.encode("surname","UTF-8")+"="+URLEncoder.encode(surname,"UTF-8")+"&"
                    +URLEncoder.encode("phonenumber","UTF-8")+"="+URLEncoder.encode(phonenumber,"UTF-8");
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
        if (result.equals("Your data updated")) {
            source.updateBWPE();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}