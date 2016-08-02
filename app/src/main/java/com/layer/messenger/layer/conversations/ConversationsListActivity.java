package com.layer.messenger.layer.conversations;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.layer.atlas.AtlasConversationsRecyclerView;
import com.layer.atlas.adapters.AtlasConversationsAdapter;
import com.layer.atlas.util.views.SwipeableItem;
import com.layer.messenger.R;
import com.layer.messenger.layer.base.LayerActivity;
import com.layer.messenger.layer.messages.MessagesListActivity;
import com.layer.messenger.layer.base.client.LayerProvider;
import com.layer.messenger.layer.push.PushNotificationReceiver;
import com.layer.messenger.util.Log;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.squareup.picasso.Picasso;

public class ConversationsListActivity extends LayerActivity {
    public ConversationsListActivity() {
        super(R.layout.activity_conversations_list, R.string.title_conversations_list, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (LayerProvider.routeLogin(this)) {
                if (!isFinishing()) finish();
                return;
            }
        } catch (Exception e) {
            Log.e("Layer could not be initialized");
        }

        final AtlasConversationsRecyclerView conversationsList = (AtlasConversationsRecyclerView) findViewById(R.id.conversations_list);

        // Atlas methods
        try {
            conversationsList.init(getLayerClient(), getParticipantProvider(), Picasso.with(ConversationsListActivity.this))
                    .setInitialHistoricMessagesToFetch(20)
                    .setOnConversationClickListener(new AtlasConversationsAdapter.OnConversationClickListener() {
                        @Override
                        public void onConversationClick(AtlasConversationsAdapter adapter, Conversation conversation) {
                            Intent intent = new Intent(ConversationsListActivity.this, MessagesListActivity.class);
                            if (Log.isLoggable(Log.VERBOSE)) {
                                Log.v("Launching MessagesListActivity with existing conversation ID: " + conversation.getId());
                            }
                            intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, conversation.getId());
                            startActivity(intent);
                        }

                        @Override
                        public boolean onConversationLongClick(AtlasConversationsAdapter adapter, Conversation conversation) {
                            return false;
                        }
                    })
                    .setOnConversationSwipeListener(new SwipeableItem.OnSwipeListener<Conversation>() {
                        @Override
                        public void onSwipe(final Conversation conversation, int direction) {
                            new AlertDialog.Builder(ConversationsListActivity.this)
                                    .setMessage(R.string.alert_message_delete_conversation)
                                    .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // TODO: simply update this one conversation
                                            conversationsList.getAdapter().notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNeutralButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            conversation.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                                        }
                                    })
                                    .setPositiveButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            conversation.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                                        }
                                    })
                                    .show();
                        }
                    });
        } catch (Exception e) {
            Log.e("Layer could not be initialized");
        }

        findViewById(R.id.floating_action_button)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(ConversationsListActivity.this, MessagesListActivity.class));
                    }
                });
    }

}
