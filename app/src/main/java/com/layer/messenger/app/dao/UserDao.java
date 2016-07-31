package com.layer.messenger.app.dao;

import android.content.Context;
import android.net.Uri;

import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.messenger.BuildConfig;
import com.layer.messenger.app.dao.api.ParticipantListener;
import com.layer.messenger.app.dao.api.ParticipantsRequestCallback;
import com.layer.messenger.app.dao.api.UserRequestHandler;
import com.layer.messenger.app.model.User;
import com.layer.messenger.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public class UserDao implements ParticipantProvider {
    private final Context mContext;
    private final Queue<ParticipantListener> mParticipantListeners = new ConcurrentLinkedQueue<>();
    private final Map<String, User> mParticipantMap = new HashMap<>();
    private final AtomicBoolean mFetching = new AtomicBoolean(false);

    public UserDao(Context context) {
        mContext = context.getApplicationContext();
    }

    public UserDao setLayerAppId() {
        load();
        fetchParticipants();
        return this;
    }

    //==============================================================================================
    // Atlas ParticipantProvider
    //==============================================================================================

    @Override
    public Map<String, Participant> getMatchingParticipants(String filter, Map<String, Participant> result) {
        if (result == null) {
            result = new HashMap<>();
        }

        synchronized (mParticipantMap) {
            // With no filter, return all Participants
            if (filter == null) {
                result.putAll(mParticipantMap);
                return result;
            }

            // Filter participants by substring matching first- and last- names
            for (User p : mParticipantMap.values()) {
                boolean matches = false;
                if (p.getName() != null && p.getName().toLowerCase().contains(filter))
                    matches = true;
                if (matches) {
                    result.put(p.getId(), p);
                } else {
                    result.remove(p.getId());
                }
            }
            return result;
        }
    }

    @Override
    public Participant getParticipant(String userId) {
        synchronized (mParticipantMap) {
            User participant = mParticipantMap.get(userId);
            if (participant != null) return participant;
            fetchParticipants();
            return null;
        }
    }

    /**
     * Adds the provided Participants to this ParticipantProvider, saves the participants, and
     * returns the list of added participant IDs.
     */
    private UserDao setParticipants(Collection<User> participants) {
        List<String> newParticipantIds = new ArrayList<>(participants.size());
        synchronized (mParticipantMap) {
            for (User participant : participants) {
                String participantId = participant.getId();
                if (!mParticipantMap.containsKey(participantId))
                    newParticipantIds.add(participantId);
                mParticipantMap.put(participantId, participant);
            }
            save();
        }
        alertParticipantsUpdated(newParticipantIds);
        return this;
    }


    //==============================================================================================
    // Persistence
    //==============================================================================================

    /**
     * Loads additional participants from SharedPreferences
     */
    private boolean load() {
        synchronized (mParticipantMap) {
            String jsonString = mContext.getSharedPreferences("participants", Context.MODE_PRIVATE).getString("json", null);
            if (jsonString == null) return false;

            try {
                for (User participant : UserUtils.participantsFromJson(new JSONArray(jsonString))) {
                    mParticipantMap.put(participant.getId(), participant);
                }
                return true;
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * Saves the current map of participants to SharedPreferences
     */
    private boolean save() {
        synchronized (mParticipantMap) {
            try {
                mContext.getSharedPreferences("participants", Context.MODE_PRIVATE).edit()
                        .putString("json", participantsToJson(mParticipantMap.values()).toString())
                        .commit();
                return true;
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
            }
        }
        return false;
    }

    private String getProjectId() {
        if (BuildConfig.LAYER_APP_ID.contains("/")) {
            return Uri.parse(BuildConfig.LAYER_APP_ID).getLastPathSegment();
        } else {
            return BuildConfig.LAYER_APP_ID;
        }
    }

    //==============================================================================================
    // Network operations
    //==============================================================================================
    private UserDao fetchParticipants() {
        if (!mFetching.compareAndSet(false, true)) {
            return this;
        }
        UserRequestHandler.startRequestParticipants(
                new ParticipantsRequestCallback() {

                    @Override
                    public void participants(List<User> users) {
                        setParticipants(users);
                        mFetching.set(false);
                    }

                    @Override
                    public void error() {
                        mFetching.set(false);
                    }
                }
        );
        return this;
    }

    //==============================================================================================
    // Utils
    //==============================================================================================


    public static JSONArray participantsToJson(Collection<User> participants) throws JSONException {
        JSONArray participantsArray = new JSONArray();
        for (User participant : participants) {
            JSONObject participantObject = new JSONObject();
            participantObject.put("id", participant.getId());
            participantObject.put("name", participant.getName());
            participantsArray.put(participantObject);
        }
        return participantsArray;
    }

    private UserDao registerParticipantListener(ParticipantListener participantListener) {
        if (!mParticipantListeners.contains(participantListener)) {
            mParticipantListeners.add(participantListener);
        }
        return this;
    }

    private UserDao unregisterParticipantListener(ParticipantListener participantListener) {
        mParticipantListeners.remove(participantListener);
        return this;
    }

    private void alertParticipantsUpdated(Collection<String> updatedParticipantIds) {
        for (ParticipantListener listener : mParticipantListeners) {
            listener.onParticipantsUpdated(this, updatedParticipantIds);
        }
    }


}