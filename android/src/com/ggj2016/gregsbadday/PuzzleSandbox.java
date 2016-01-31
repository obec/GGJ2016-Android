package com.ggj2016.gregsbadday;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Network;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PuzzleSandbox extends AppCompatActivity {
    private Bitmap bitmap = null;

    @Bind(R.id.trace_view) TraceView traceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        ButterKnife.bind(this);

        // Variable for checking the connection status
        boolean internetConnection = false;

        // Check connection with the server
        internetConnection = checkInternetConnection();
        if(internetConnection) {
            String urlToConnect = "https://voodoo.madsciencesoftware.com";
            postServer(urlToConnect);
        }
    }

    // Function to check the connection with the server
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connection =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        Network[] networks = connection.getAllNetworks();
        NetworkInfo networkInfo;

        for (Network mNetwork : networks) {
            networkInfo = connection.getNetworkInfo(mNetwork);

            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                Toast.makeText(this, " Connected ", Toast.LENGTH_SHORT).show();
                return true;
            }else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)){
                Toast.makeText(this, " Disconnected ", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return false;
    }

    // Android doesn't like network operations in the UI thread.
    // This function acts a wrapper for the actual HTTP connection.
    // A new thread is created in this function for the Network Operations
    private void getServer(String urlStr)
    {
        final String url = urlStr;
        new Thread() {
            public void run() {
                InputStream inputStream = null;

                try {
                    // Call the function to do the get
                    inputStream = getHttpConnection(url);

                    // Close the input stream if data was received
                    if(inputStream != null) {
                        Log.d("Data", "Received");
                        inputStream.close();
                    }
                }

                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

    // Wrapper for the post
    private void postServer(String urlStr)
    {
        final String url = urlStr;
        new Thread() {
            public void run() {
                InputStream inputStream = null;

                try {
                    // Call the function to do the post
                    inputStream = postHttpConnection(url);

                    // Close the input stream if data was received
                    if(inputStream != null) {
                        Log.d("Data", "Posted");
                        BufferedReader bufferedReader =
                                new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
                        String line = null;

                        while ((line = bufferedReader.readLine()) != null) {
                            Log.d("Output: ", line);
                        }
                        bufferedReader.close();
                    }
                }

                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // Function which does the work for "GET"
    private InputStream getHttpConnection(String urlStr) {
        InputStream inputStream = null;
        int responseCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConnection = url.openConnection();

            // Sanity check to see if the URL is a HTTP URL
            if (!(urlConnection instanceof HttpURLConnection)) {
                throw new IOException("URL is not an HTTP URL");
            }

            // Connect to the server with user interaction disabled and redirection enabled
            HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
            httpConnection.setAllowUserInteraction(false);
            httpConnection.setInstanceFollowRedirects(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();
            responseCode = httpConnection.getResponseCode();

            // Check to see if the connection to the server was okay
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConnection.getInputStream();
                Log.d("HTTP Get", "OK");
            }
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        // Return the stream from the server
        return inputStream;
    }

    // Function which does the work for "POST"
    private InputStream postHttpConnection(String urlStr){
        InputStream inputStream = null;
        int responseCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConnection = url.openConnection();

            // Sanity check to see if the URL is a HTTP URL
            if (!(urlConnection instanceof HttpURLConnection)) {
                throw new IOException("URL is not an HTTP URL");
            }

            // Connect to the server with user interaction disabled and redirection enabled
            // and set up to send bytes
            HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
            httpConnection.setAllowUserInteraction(false);
            httpConnection.setInstanceFollowRedirects(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpConnection.connect();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("target", "body");
            jsonObject.put("value", 12345);


            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes("UTF-8"));
            outputStream.flush();

            responseCode = httpConnection.getResponseCode();

            // Check to see if there is a response 200 from the server
            if (responseCode == 200) {
                inputStream = httpConnection.getInputStream();
                Log.d("Response Code", ""+responseCode);
                Log.d("HTTP Post", "OK");
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return the stream from the server
        return inputStream;
    }

    @OnClick(R.id.clear_button)
    void onClearButtonClicked(View view) {
        traceView.clearDrawing();
    }
}
