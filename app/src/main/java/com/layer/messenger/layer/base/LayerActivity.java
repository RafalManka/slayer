package com.layer.messenger.layer.base;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.layer.atlas.provider.ParticipantProvider;
import com.layer.messenger.layer.base.client.LayerProvider;
import com.layer.messenger.util.Log;
import com.layer.sdk.LayerClient;

/**
 * for keeping activities DRY.
 */
public abstract class LayerActivity extends AppCompatActivity {
    private final int mLayoutResId;
    private final int mMenuTitleResId;
    private final boolean mMenuBackEnabled;

    public LayerActivity(int layoutResId, int menuTitleResId, boolean menuBackEnabled) {
        mLayoutResId = layoutResId;
        mMenuTitleResId = menuTitleResId;
        mMenuBackEnabled = menuBackEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutResId);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (mMenuBackEnabled) actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mMenuTitleResId);
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            super.setTitle(title);
        } else {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            super.setTitle(titleId);
        } else {
            actionBar.setTitle(titleId);
        }
    }

    /**
     * Attempt ro connect to layer and authenticate. every time the Activity is started.
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            LayerClient client = LayerProvider.getInstance();
            if (client.isAuthenticated()) {
                client.connect();
            } else {
                client.authenticate();
            }
        } catch (Exception e) {
            Log.e("layer could not be initialized.");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Menu "Navigate Up" acts like hardware back button
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected LayerClient getLayerClient() throws Exception {
        return LayerProvider.getInstance();
    }

    protected ParticipantProvider getParticipantProvider() throws Exception {
        return LayerProvider.getParticipantProvider();
    }

}
