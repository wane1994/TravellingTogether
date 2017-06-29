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

import ru.travellingtogether.travellingtogether.fragments.FragmentRegister;

// sending JSON request to server and getting answer
// Registration stage: send new user data to database
public class RegisterBW extends AsyncTask<String,Void,String> {
    Context context;
    ProgressDialog loading;
    public FragmentRegister source = null;

    public RegisterBW(FragmentRegister fr, Context ctx) {
        source = fr;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String register_url = "https://travelling-together.000webhostapp.com/php/register.php";

        try {
            String username = params[0];
            String password = params[1];
            String name = params[2];
            String surname = params[3];
            String phonenumber = params [4];
            URL url = new URL(register_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // sending a request to server to insert data in database
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                    +URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8")+"&"
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
        if (result.equals("Registration successful")) {
            source.registerBWPE();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}