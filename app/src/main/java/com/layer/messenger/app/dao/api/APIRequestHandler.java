package com.layer.messenger.app.dao.api;

import android.net.Uri;
import android.os.AsyncTask;

import com.layer.messenger.BuildConfig;
import com.layer.messenger.app.dao.UserUtils;
import com.layer.messenger.layer.base.client.LayerProvider;
import com.layer.messenger.util.Log;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.layer.messenger.util.Util.streamToString;

/**
 * Created by rafal on 7/31/16.
 */
public class APIRequestHandler {

    private static String getProjectId() {
        if (BuildConfig.LAYER_APP_ID.contains("/")) {
            return Uri.parse(BuildConfig.LAYER_APP_ID).getLastPathSegment();
        } else {
            return BuildConfig.LAYER_APP_ID;
        }
    }

    /**
     * Send a request to your private API and request for participants that have conversations
     * with current user.
     * The response should include usernames and avatars as well as id's
     *
     * @param callback
     */
    public static void startRequestParticipants(final ParticipantsRequestCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                try {
                    // Post request
                    String url = "https://layer-identity-provider.herokuapp.com/apps/" + getProjectId() + "/atlas_identities";
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(false);
                    connection.setRequestMethod("GET");
                    connection.addRequestProperty("Content-Type", "application/json");
                    connection.addRequestProperty("Accept", "application/json");
                    connection.addRequestProperty("X_LAYER_APP_ID", getProjectId());
                    // Handle failure
                    int statusCode = connection.getResponseCode();
                    if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                        if (Log.isLoggable(Log.ERROR)) {
                            Log.e("Got status " + statusCode + " when fetching participants");
                        }
                        return null;
                    }
                    // Parse response
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    String result = streamToString(in);
                    in.close();
                    connection.disconnect();
                    JSONArray json = new JSONArray(result);
                    // callback
                    callback.participants(UserUtils.participantsFromJson(json));
                } catch (Exception e) {
                    if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
                    callback.error();
                }
                return null;
            }
        }.execute();
    }

    /**
     * Authenticate user in backend. Read more about it here: https://developer.layer.com/docs/android/guides
     * Basically you need to send credentials (including nonce) to your backend.
     *
     * @param layerClient reference to the client
     * @param credentials username and nonce in this case
     * @param callback    for async communication
     */
    public static void startRequestAuthenticate(LayerClient layerClient, JSONObject credentials, OnAuthenticationFailedListener callback) {
        try {
            // Post request
            String url = "https://layer-identity-provider.herokuapp.com/apps/" + getProjectId() + "/atlas_identities";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("X_LAYER_APP_ID", getProjectId());
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            OutputStream os = connection.getOutputStream();
            os.write(credentials.toString().getBytes("UTF-8"));
            os.close();

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = "Got status " + statusCode + " when requesting authentication for '";
                error += credentials.get("name") + "' with nonce '" + credentials.get("nonce");
                error += "' from '" + url + "'";
                if (Log.isLoggable(Log.ERROR)) Log.e(error);
                callback.onError(error);
                return;
            }

            // Parse response
            InputStream in = new BufferedInputStream(connection.getInputStream());
            String result = streamToString(in);
            in.close();
            connection.disconnect();
            JSONObject json = new JSONObject(result);
            if (json.has("error")) {
                String error = json.getString("error");
                if (Log.isLoggable(Log.ERROR)) Log.e(error);
                callback.onError(error);
                return;
            }

            // Answer authentication challenge.
            String identityToken = json.optString("identity_token", null);
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Got identity token: " + identityToken);
            layerClient.answerAuthenticationChallenge(identityToken);

        } catch (Exception e) {
            String error = "Error when authenticating with provider: " + e.getMessage();
            if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
            callback.onError(error);
        }
    }

}
