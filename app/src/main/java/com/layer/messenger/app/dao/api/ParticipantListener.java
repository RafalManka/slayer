package com.layer.messenger.app.dao.api;

import com.layer.messenger.app.dao.UserDao;

import java.util.Collection;

/**
 * Created by rafal on 7/31/16.
 */
public interface ParticipantListener {
    void onParticipantsUpdated(UserDao provider, Collection<String> updatedParticipantIds);
}
