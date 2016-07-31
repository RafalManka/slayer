package com.layer.messenger.app.dao.api;

import com.layer.messenger.app.model.User;

import java.util.List;

/**
 * Created by rafal on 7/31/16.
 */
public interface ParticipantsRequestCallback {

    void participants(List<User> users);

    void error();

}