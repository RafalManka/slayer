package com.layer.messenger.app.dao;

import android.net.Uri;

import com.layer.messenger.BuildConfig;
import com.layer.messenger.app.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafal on 7/31/16.
 */
public class UserUtils {

    public static List<User> participantsFromJson(JSONArray participantArray) throws JSONException {
        List<User> participants = new ArrayList<>(participantArray.length());
        for (int i = 0; i < participantArray.length(); i++) {
            JSONObject participantObject = participantArray.getJSONObject(i);
            User participant = new User();
            participant.setId(participantObject.optString("id"));
            participant.setName(participantObject.optString("name"));
            participant.setAvatarUrl(null);
            participants.add(participant);
        }
        return participants;
    }

    public static String getProjectId() {
        if (BuildConfig.LAYER_APP_ID.contains("/")) {
            return Uri.parse(BuildConfig.LAYER_APP_ID).getLastPathSegment();
        } else {
            return BuildConfig.LAYER_APP_ID;
        }
    }
}
