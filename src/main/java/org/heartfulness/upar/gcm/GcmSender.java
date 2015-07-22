package org.heartfulness.upar.gcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class GcmSender {
    
    public static final String API_KEY = "AIzaSyBc_qArgXgt-8d-hVSPfc-a8i4M38wAU7k";
    
    public void sendMessage(String topic, String msg) {
        try {
            // Prepare JSON containing the GCM message content. What to send and where to send.
            JSONObject jGcmData = new JSONObject();
            
            // Where to send GCM message.
            jGcmData.put("to", "/topics/" + topic);
            
            // What to send in GCM message to android device.
            JSONObject jData = new JSONObject();
            jData.put("message", msg.trim());            
            jGcmData.put("data", jData);
            
            // What to send in GCM message to ios device.
            JSONObject notification = new JSONObject();
            notification.put("body", msg.trim());
            jGcmData.put("notification", notification);
            
            // Create connection to send GCM Message request.
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
            System.out.println("Check your device/emulator for notification or logcat for " +
                    "confirmation of the receipt of the GCM message.");
        } catch (IOException e) {
            System.out.println("Unable to send GCM message.");
            System.out.println("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
            e.printStackTrace();
        }
    }

}
