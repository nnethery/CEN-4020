package com.example.zanj.cen4020;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Noah on 11/18/17.
 */

    public class FirebaseIDService extends FirebaseInstanceIdService {
        private static final String TAG = "com.example.zanj.cen4020.FirebaseIDService";

        @SuppressLint("LongLogTag")
        @Override
        public void onTokenRefresh() {
            // Get updated InstanceID token.
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "Refreshed token: " + refreshedToken);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(refreshedToken);
        }

        /**
         * Persist token to third-party servers.
         *
         * Modify this method to associate the user's FCM InstanceID token with any server-side account
         * maintained by your application.
         *
         * @param token The new token.
         */
        private void sendRegistrationToServer(String token) {
            // Add custom implementation, as needed.
        }
    }
