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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.heartfulness.upar.client.MainActivity;
import org.heartfulness.upar.client.util.BadgeUtil;

import org.heartfulness.upar.client.R;
import org.json.JSONException;
import org.json.JSONObject;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    public static final String FILENAME = "hello_file";

    public static final AtomicInteger count = new AtomicInteger();

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = '\n' + data.getString("message") + '\n';
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        try {
//            Intent i = new Intent(getBaseContext(), MainActivity.class);
//            startActivity(i);
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(message.getBytes());
            fos.close();
        } catch (Exception e) {
            // do Nothing
        }
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        String msg = parse(message);
        if(msg == null) { // parse says nothing to notify
            return;
        }
        playNotification(msg);
        BadgeUtil.setBadge(getBaseContext(), count.incrementAndGet());
    }

    private void playNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private String parse(String message) {
        String messageText = null;
        try {
            JSONObject command = new JSONObject(message);
            String status = command.getString("submit");
            if("sharePair".equalsIgnoreCase(status)) {
                String pairId = command.getString("message");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putString(QuickstartPreferences.CHAT_PAIR_ID, pairId).apply();

                Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
                registrationComplete.putExtra("COMMAND", "CHAT");
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
            } else if("start".equalsIgnoreCase(status)) {
                String pairId = command.getString("message");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putString(QuickstartPreferences.CHAT_PAIR_ID, pairId).apply();
                MediaPlayer mp = MediaPlayer.create(this,
                        R.raw.start);
                mp.setVolume(1.0f, 1.0f);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.start();
                messageText = null;
                Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
                registrationComplete.putExtra("MESSAGE", "PLEASE START MEDITATION");
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
            } else if("end".equalsIgnoreCase(status)) {
                String pairId = command.getString("message");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().remove(QuickstartPreferences.CHAT_PAIR_ID).apply();
                messageText = null;
            } else if("chat".equalsIgnoreCase(status)) {
                String m = command.getString("message");

                Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
                registrationComplete.putExtra("MESSAGE", m);
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

                messageText = null;
            } else {
                messageText = command.getString("message");
            }
        } catch (JSONException e) {
            // couldn't parse it as a JSON message. So, not a command
            messageText = message;
        }
        return messageText;
    }
}
