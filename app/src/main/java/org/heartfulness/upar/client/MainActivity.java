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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


import org.heartfulness.upar.client.chat.DiscussArrayAdapter;
import org.heartfulness.upar.client.chat.OneComment;
import org.heartfulness.upar.client.handler.ServiceHandler;
import org.heartfulness.upar.client.util.BadgeUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private DiscussArrayAdapter adapter;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private ListView lv;
    private Button button;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyGcmListenerService.count.set(0);
//        String path = "http://heartfulness.org/wp-content/uploads/2015/06/HFN_Logo_Opener_Long_20150627_mix.mp4";
//        VideoView videoView = (VideoView)findViewById(R.id.VideoView);
//        Uri uri=Uri.parse(path);
//        videoView.setVideoURI(uri);
//        videoView.start();
        BadgeUtil.setBadge(getBaseContext(), MyGcmListenerService.count.get());
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                mInformationTextView = (TextView) findViewById(R.id.informationTextView);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
                String m = intent.getStringExtra("MESSAGE");
                if(m != null) {
                    adapter.add(new OneComment(true, m));
                }
                String command = intent.getStringExtra("COMMAND");
                if("CHAT".equalsIgnoreCase(command)){
                    LinearLayout table = (LinearLayout) findViewById(R.id.form);
                    table.setVisibility(View.VISIBLE);

                    lv = (ListView) findViewById(R.id.listView1);

                    adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.listitem_discuss);

                    lv.setAdapter(adapter);
                }
            }
        };

        startIntent(null);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.togglebutton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Bundle b = new Bundle();
                String notification;
                if (isChecked) {
                    // The toggle is enabled
                    notification = "yes";
                } else {
                    // The toggle is disabled
                    notification = "no";
                }
                b.putString("notification", notification);
                startIntent(b);
            }
        });

        refreshScreen(sharedPreferences);
    }

    private void refreshScreen(SharedPreferences sharedPreferences) {

        String type = sharedPreferences.getString(QuickstartPreferences.AUTH_TOKEN_TYPE, "");
        if(type == null || "".equals(type)) {

            // need to register
            button = (Button) findViewById(R.id.registerButton);

            TextView fullname = (TextView) findViewById(R.id.fullname);
            fullname.setVisibility(View.VISIBLE);
            TextView abhyasiid = (TextView) findViewById(R.id.abhyasiid);
            abhyasiid.setVisibility(View.VISIBLE);
            RadioGroup typeOfUser = (RadioGroup) findViewById(R.id.radios);
            typeOfUser.setVisibility(View.VISIBLE);

        } else {
            Button b = (Button) findViewById(R.id.registerButton);
            b.setVisibility(View.GONE);
            if("PREFECT".equalsIgnoreCase(type)) {
                button = (Button) findViewById(R.id.giveSittingButton);
                ToggleButton tb = (ToggleButton) findViewById(R.id.togglebutton);
                tb.setVisibility(View.VISIBLE);
            } else if("ABHYASI".equalsIgnoreCase(type)) {
                button = (Button) findViewById(R.id.getSittingButton);
            } else if("NONE".equalsIgnoreCase(type)) {
                // TODO add Take introductory sitting button??
                Log.i(TAG, "User selected None as option");
            }
        }

        if(button != null) {
            button.setVisibility(View.VISIBLE);
        }

        // notification switch
        String notify = sharedPreferences.getString(QuickstartPreferences.NOTIFICATION, "");
        if(notify != null && "yes".equalsIgnoreCase(notify)) {
            ToggleButton tb = (ToggleButton) findViewById(R.id.togglebutton);
            tb.setChecked(true);
        }

    }


    private void startIntent(Bundle b) {
        mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            if(b!= null) {
                intent.putExtras(b);
            }
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /** Called when the user clicks the Send button */
    public void sendRegister(View view) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String regId = sharedPreferences
                .getString(QuickstartPreferences.AUTH_TOKEN_SENT_BY_SERVER, "");
        TextView fullname = (TextView) findViewById(R.id.fullname);
        TextView abhyasiid = (TextView) findViewById(R.id.abhyasiid);

        new RegisterUser(getBaseContext(), regId, fullname.getText().toString(), abhyasiid.getText().toString(), type).execute();
    }

    /** Called when the user clicks the Send button */
    public void sendGetSitting(View view) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String regId = sharedPreferences
                .getString(QuickstartPreferences.AUTH_TOKEN_SENT_BY_SERVER, "");
        new Register(getString(R.string.sitting_server_url) + "/getSitting",regId, null).execute();
    }

    /** Called when the user clicks the Send button */
    public void sendGiveSitting(View view) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String regId = sharedPreferences
                .getString(QuickstartPreferences.AUTH_TOKEN_SENT_BY_SERVER, "");
        new Register(getString(R.string.sitting_server_url) + "/giveSitting",regId, null).execute();
    }
    /** Called when user clicks the Send button in chat window */
    public void sendMessage(View view) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String regId = sharedPreferences
                .getString(QuickstartPreferences.AUTH_TOKEN_SENT_BY_SERVER, "");
        String pairId = sharedPreferences
                .getString(QuickstartPreferences.CHAT_PAIR_ID, "");
        TextView message = (TextView) findViewById(R.id.message);
        String payload;
        try {
            String msg = message.getText().toString().trim();
            payload = "pairId=" + pairId + "&msg=" + URLEncoder.encode(msg, "UTF-8");
            new Register(getString(R.string.sitting_server_url) + "/send", regId, payload).execute();
            adapter.add(new OneComment(false, msg));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            mInformationTextView.setText("Failed while sending message");
        }
        message.setText("");

    }

    /** Called when the user chooses the Radio button */
    public void onRadioButtonClicked(View view) {

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_abhyasi:
                if (checked) {
                   type = getString(R.string.abhyasi);
                }
                break;
            case R.id.radio_prefect:
                if (checked) {
                    type = getString(R.string.prefect);
                }
                break;
            case R.id.radio_ninja:
                if (checked) {
                    type = getString(R.string.none);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class Register extends AsyncTask<Void, Void, Void> {

        private final String regId;
        private final String URL;
        private final String payload;

        public Register(String URL, String regId, String payload) {
            this.regId = regId;
            this.payload = payload;
            this.URL = URL;
        }

        @Override
        protected void onPreExecute() {
            mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String SERVER_URL = URL + "?regId=" + regId;
            if(payload !=null) {
                SERVER_URL += "&" + payload;
            }
            ServiceHandler handler = new ServiceHandler();
            String jsonResults = handler.makeServiceCall(SERVER_URL, ServiceHandler.GET);
            try {
                if (jsonResults != null) {
                    Log.i(TAG, jsonResults);
                }
            } catch(Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class RegisterUser extends AsyncTask<Void, Void, Void> {

        private final String regId;
        private final String fullname;
        private final String abhyasiid;
        private final String type;
        private final Context context;
        private String authToken;
        private ArrayList<String> TOPICS;
        private String actualType;
        private String notification;

        public RegisterUser(Context context, String regId, String fullname, String abhyasiid, String type) {
            this.regId = regId;
            this.fullname = fullname.trim();
            this.abhyasiid = abhyasiid.trim();
            this.type = type.trim();
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String SERVER_URL = getString(R.string.server_url)+"/registerUser?" +
                    "regId=" + regId + "&fullname=" + fullname +
                    "&abhyasiid=" + abhyasiid + "&type=" + type;
            ServiceHandler handler = new ServiceHandler();
            String jsonResults = handler.makeServiceCall(SERVER_URL, ServiceHandler.GET);
            try {
                if (jsonResults != null) {
                    JSONObject results = new JSONObject(jsonResults);
                    authToken = results.getString("authToken");
                    String type = results.getString("type");
                    notification = results.getString("notification");
                    JSONArray jsonArray = results.getJSONArray("topics");
                    ArrayList<String> list = new ArrayList<>();
                    for (int i=0; i<jsonArray.length(); i++) {
                        list.add( jsonArray.getString(i) );
                    }
                    TOPICS = list;
                    actualType = type;
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
            sharedPreferences.edit().putString(QuickstartPreferences.AUTH_TOKEN_TYPE, actualType).apply();

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            //otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();

            // Subscription status
            Bundle b = new Bundle();
            b.putStringArrayList("subscribe", TOPICS);
            startIntent(b);

            // so the progress indicator can be hidden.
            TextView fullname = (TextView) findViewById(R.id.fullname);
            fullname.setVisibility(View.GONE);
            TextView abhyasiid = (TextView) findViewById(R.id.abhyasiid);
            abhyasiid.setVisibility(View.GONE);
            RadioGroup type = (RadioGroup) findViewById(R.id.radios);
            type.setVisibility(View.GONE);
            if("yes".equalsIgnoreCase(notification)) {
                sharedPreferences.edit().putString(QuickstartPreferences.NOTIFICATION, notification).apply();
                ToggleButton tb = (ToggleButton) findViewById(R.id.togglebutton);
                tb.setChecked(true);
            }
            mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
            refreshScreen(sharedPreferences);
        }
    }

}
