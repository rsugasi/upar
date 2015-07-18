/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.heartfulness.upar.client;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

import java.util.ArrayList;

import org.heartfulness.upar.client.handler.ServiceHandler;


public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);

                // send registration to app's servers.
                sendRegistrationToServer(token, sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false));
                // [END register_for_gcm]

                // Subscribe to topic channels
                Bundle b = intent.getExtras();
                if(b != null) { // some one sent an intent
                    ArrayList<String> topics = b.getStringArrayList("subscribe");
                    if(topics!=null && topics.size() > 0) {
                        // Subscription status
                        Intent subscription = new Intent(this, SubscriptionIntentService.class);
                        subscription.putExtra("token", token);
                        subscription.putExtra("topics", topics.toArray(new String[topics.size()]));
                        startService(subscription);
                    }
                    String notification = b.getString("notification");
                    ArrayList<String> TOPICS = new ArrayList<>();
                    TOPICS.add("prefect");
                    if("yes".equalsIgnoreCase(notification) || "no".equalsIgnoreCase(notification)) {
                        sendSubscription(sharedPreferences, notification, token, TOPICS);
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendSubscription(SharedPreferences sharedPreferences, String notification, String token, ArrayList<String> topics) {
        sharedPreferences.edit().putString("NOTIFY", notification).apply();
        Intent subscription = new Intent(this, SubscriptionIntentService.class);
        subscription.putExtra("token", token);
        subscription.putExtra("topics", topics.toArray(new String[topics.size()]));
        subscription.putExtra("notify", notification);
        startService(subscription);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, boolean isSentToServer) {
        // Add custom implementation, as needed.
        if(!isSentToServer) {
            new RegisterToken(this, token).execute();
        } else {
            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(this.getBaseContext()).sendBroadcast(registrationComplete);
        }
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class RegisterToken extends AsyncTask<Void, Void, Void> {

        private final String regId;
        private final Context context;
        private String authToken;

        public RegisterToken(Context context, String regId) {
            this.regId = regId;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String SERVER_URL = getString(R.string.server_url)+"/registerDevice?registrationId=" + Base64.encodeToString(regId.getBytes(), Base64.NO_WRAP);
            ServiceHandler handler = new ServiceHandler();
            String jsonResults = handler.makeServiceCall(SERVER_URL, ServiceHandler.GET);
            try {
                if (jsonResults != null) {
                    JSONObject results = new JSONObject(jsonResults);
                    authToken = results.getString("authToken");

                }
            } catch(Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putString(QuickstartPreferences.AUTH_TOKEN_SENT_BY_SERVER, authToken).apply();

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            //otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();

            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(context).sendBroadcast(registrationComplete);
        }
    }

}
