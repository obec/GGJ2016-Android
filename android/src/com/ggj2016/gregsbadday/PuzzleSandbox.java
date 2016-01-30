package com.ggj2016.gregsbadday;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Network;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class PuzzleSandbox extends AppCompatActivity {
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        // Check connection with the server
        checkInternetConenction();
        connectToServer("https://voodoo.madsciencesoftware.com");
    }

    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        Network[] networks = connec.getAllNetworks();
        NetworkInfo networkInfo;

        for (Network mNetwork : networks) {
            networkInfo = connec.getNetworkInfo(mNetwork);

            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
                return true;
            }else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)){
                Toast.makeText(this, " DisConnected ", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return false;
    }

    // Android doesn't like network operations in the UI thread.
    // This function acts a wrapper for the actual HTTP connection.
    // A new thread is created in this function for the Network Operations
    private void connectToServer(String urlStr)
    {
        final String url = urlStr;
        new Thread() {
            public void run() {
                InputStream inputStream = null;

                try {
                    inputStream = openHttpConnection(url);

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

    private InputStream openHttpConnection(String urlStr) {
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
                Log.d("HTTP", "OK");
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
}
