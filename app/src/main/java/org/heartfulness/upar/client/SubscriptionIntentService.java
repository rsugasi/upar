package org.heartfulness.upar.client;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;


/**
 * Created by rsugasi on 7/16/15.
 */
public class SubscriptionIntentService extends IntentService {

    private static final String TAG = "SubscriptionService";

    public SubscriptionIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String token = intent.getStringExtra("token");
        String[] topics = intent.getStringArrayExtra("topics");
        String subscribe = intent.getStringExtra("notify");
        if("no".equalsIgnoreCase(subscribe)) {
            unsubscribeTopics(token, topics);
        } else {
            subscribeTopics(token, topics);
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token, String[] TOPICS) {
        if(TOPICS != null) {
            for (String topic : TOPICS) {
                try {
                    GcmPubSub pubSub = GcmPubSub.getInstance(this);
                    if("ABHYASI".equalsIgnoreCase(topic)) {
                        pubSub.unsubscribe(token, "/topics/prefect");
                    } else if("PREFECT".equalsIgnoreCase(topic)) {
                        pubSub.unsubscribe(token, "/topics/abhyasi");
                    }
                    pubSub.subscribe(token, "/topics/" + topic, null);
                } catch(Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
        }
    }
    // [END subscribe_topics]

    private void unsubscribeTopics(String token, String[] TOPICS) {
        if(TOPICS != null) {
            for(String topic : TOPICS) {
                try {
                    GcmPubSub pubSub = GcmPubSub.getInstance(this);
                    pubSub.unsubscribe(token, "/topics/"+ topic);
                } catch(Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
        }
    }
}
