package com.ggj2016.gregsbadday;

import android.util.Log;

import com.ggj2016.gregsbadday.messages.GameStateMessage;
import com.ggj2016.gregsbadday.messages.PinMessage;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import timber.log.Timber;

public class NetworkManager {

    private static final String BASE_ENDPOINT = "https://voodoo.madsciencesoftware.com";

    public interface Listener {
        void onSuccess(GameStateMessage message);
        void onError();
    }

    private static Gson mGson = new Gson();

    // Android doesn't like network operations in the UI thread.
    // This function acts a wrapper for the actual HTTP connection.
    // A new thread is created in this function for the Network Operations
    public static void getServer(String urlStr) {
        final String url = urlStr;
        new Thread() {
            public void run() {
                InputStream inputStream = null;

                try {
                    // Call the function to do the get
                    inputStream = getHttpConnection(url);

                    // Close the input stream if data was received
                    if (inputStream != null) {
                        Log.d("Data", "Received");
                        inputStream.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

    // Wrapper for the post
    public static void postServer(final PinMessage pinMessage, final Listener listener) {
        new Thread() {
            public void run() {
                InputStream inputStream = null;

                try {
                    // Call the function to do the post
                    inputStream = postHttpConnection(BASE_ENDPOINT, pinMessage);

                    // Close the input stream if data was received
                    if (inputStream != null) {
                        Log.d("Data", "Posted");
                        BufferedReader bufferedReader =
                                new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

                        GameStateMessage state = mGson.fromJson(bufferedReader, GameStateMessage.class);
                        listener.onSuccess(state);

                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    Timber.e(e, "Error parsing response.");
                    listener.onError();
                }
            }
        }.start();
    }

    // Function which does the work for "GET"
    public static InputStream getHttpConnection(String urlStr) {
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
        } catch (IOException e) {
            Timber.e(e, "Error getting response from server.");
        }

        // Return the stream from the server
        return inputStream;
    }

    // Function which does the work for "POST"
    public static InputStream postHttpConnection(String urlStr, PinMessage pinMessage) {
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
            Timber.d("Connecting");
            httpConnection.connect();

            String serializedPinMessage = mGson.toJson(pinMessage);

            Timber.d("Getting output stream");
            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(serializedPinMessage.getBytes("UTF-8"));
            outputStream.flush();

            Timber.d("Getting response code");
            responseCode = httpConnection.getResponseCode();

            // Check to see if there is a response 200 from the server
            if (responseCode == 200) {
                inputStream = httpConnection.getInputStream();
                Log.d("Response Code", "" + responseCode);
                Log.d("HTTP Post", "OK");
            } else {
                Timber.w("Unexpected response code: %d", responseCode);
            }
        } catch (IOException e) {
            Timber.e(e, "Error posting pin message.");
        }

        // Return the stream from the server
        return inputStream;
    }

}
