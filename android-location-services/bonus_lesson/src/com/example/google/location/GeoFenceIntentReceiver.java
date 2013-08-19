/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.google.location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoFenceIntentReceiver receives geofence triggers and calls startAcivity
 * which updates the UI.
 */
public class GeoFenceIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(LocationActivity.ACTION_GEOFENCE)) {
            return;
        }
        boolean hasError = LocationClient.hasError(intent);
        if (hasError) {
            // This is an intent that indicates error.
            Log.v(LocationActivity.TAG, "hasError == true");
            return;
        }
        int transition = LocationClient.getGeofenceTransition(intent);
        List<Geofence> list = LocationClient.getTriggeringGeofences(intent);
        if (transition == -1 || list == null) {
            Log.v(LocationActivity.TAG, "list == null OR " + transition);
            return;
        }
        Log.v(LocationActivity.TAG, "geo_fence transition == " + transition);
        ArrayList<String> requestIds = new ArrayList<String>();
        for (Geofence geoFence : list) {
            requestIds.add(geoFence.getRequestId());
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("request_ids", requestIds);

        // Create a new intent and set extra arguments which contain the
        // request_ids of geofences triggered and corresponding transition.
        Intent myIntent = new Intent(context, LocationActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra("RECEIVER_STARTED", true);
        myIntent.putExtra("geo_fences", bundle);
        myIntent.putExtra("transition", transition);

        if (LocationActivity.isAppForeground) {
            context.startActivity(myIntent);
        } else {
            // Send a notification when the app is in the background
        }
    }
}
