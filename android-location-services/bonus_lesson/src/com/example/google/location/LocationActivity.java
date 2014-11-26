/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.google.location;

import com.example.google.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationActivity extends FragmentActivity {
    public static String TAG = "LocationActivity";
    public static boolean isAppForeground = false;
    private static final int ERROR_DIALOG_ON_CREATE_REQUEST_CODE = 4055;
    private static final int ERROR_DIALOG_ON_RESUME_REQUEST_CODE = 4056;

    // Shared variables
    private GoogleMap mMap;
    private Dialog errorDialog;

    // Location Request variables
    private LocationClient mLocationClient;
    private TextView mLocationStatus;
    private LocationCallback mLocationCallback = new LocationCallback();
    private Location mLastLocation;
    private static final int LOCATION_UPDATES_INTERVAL = 10000; // Setting 10
                                                                // sec interval
                                                                // for location
                                                                // updates

    // Activity Recognition variables
    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivityRecognitionCallback mActivityRecognitionCallback = new ActivityRecognitionCallback();
    public static final String ACTION_ACTIVITY_RECOGNITION =
            "com.example.google.location.LocationActivity.ACTIVITY_RECOGNITION";
    private static final int ACTIVITY_UPDATES_INTERVAL = 4000;
    private PendingIntent mActivityRecognitionPendingIntent;
    private Switch mSwitch;
    private ActivityRecognitionIntentReceiver mActivityRecognitionIntentReceiver;

    // Geo Fencing variables
    private GeoFenceCallback mGeoFenceCallback = new GeoFenceCallback();
    private int id = 0;
    private static final float GEOFENCE_RADIUS = 100;
    private HashMap<String, Circle> mGeoFences;
    private HashMap<String, Circle> mTriggeringFences;
    public static final String ACTION_GEOFENCE =
            "com.example.google.location.LocationActivity.GEOFENCE";
    private TextView mGeoFenceStatus;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        checkGooglePlayServiceAvailability(ERROR_DIALOG_ON_CREATE_REQUEST_CODE);
    }

    private void init() {
        // Initialize map
        if (mMap == null) {
            FragmentManager myFragmentManager = getSupportFragmentManager();
            SupportMapFragment myMapFragment = (SupportMapFragment) myFragmentManager
                    .findFragmentById(R.id.map);
            mMap = myMapFragment.getMap();
        }

        // Initialize Location Client
        mLocationStatus = (TextView) findViewById(R.id.location_status);

        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this, mLocationCallback, mLocationCallback);
            Log.v(LocationActivity.TAG, "Location Client connect");
            if (!(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
                mLocationClient.connect();
            }
        }

        // Initialize Action Recognition
        if (mActivityRecognitionClient == null) {
            mActivityRecognitionClient =
                    new ActivityRecognitionClient(this,
                            mActivityRecognitionCallback, mActivityRecognitionCallback);
        }

        mSwitch = (Switch) findViewById(R.id.swtich);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startActivityDetection(buttonView);
                } else {
                    stopActivityDetection(buttonView);
                }
            }
        });

        if (mActivityRecognitionIntentReceiver == null) {
            mActivityRecognitionIntentReceiver = new ActivityRecognitionIntentReceiver();
            registerReceiver(mActivityRecognitionIntentReceiver,
                    new IntentFilter(LocationActivity.ACTION_ACTIVITY_RECOGNITION));
        }

        // Initialize Geo Fencing
        mGeoFenceStatus = (TextView) findViewById(R.id.geo_fence_status);

        if (mGeoFences == null) {
            mGeoFences = new HashMap<String, Circle>();
        }

        if (mTriggeringFences == null) {
            mTriggeringFences = new HashMap<String, Circle>();
        }

        // Setup map to allow adding Geo Fences
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMapLongClickListener(mGeoFenceCallback);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Indicate the application is in background
        isAppForeground = false;

        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Indicate the application is in foreground
        isAppForeground = true;

        checkGooglePlayServiceAvailability(ERROR_DIALOG_ON_RESUME_REQUEST_CODE);
        
        if (mLocationClient.isConnected()) {
            restartLocationClient();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mActivityRecognitionIntentReceiver);
        mActivityRecognitionIntentReceiver = null;
    }

    private void checkGooglePlayServiceAvailability(int requestCode) {
        // Query for the status of Google Play services on the device
        int statusCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());

        if (statusCode == ConnectionResult.SUCCESS) {
            init();
        } else {
            if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)) {
                errorDialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                        this, requestCode);
                errorDialog.show();
            } else {
                // Handle unrecoverable error
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ERROR_DIALOG_ON_CREATE_REQUEST_CODE:
                    init();
                    break;
                case ERROR_DIALOG_ON_RESUME_REQUEST_CODE:
                    restartLocationClient();
                    break;
            }
        }
    }

    private void restartLocationClient() {
        if (!(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
            mLocationClient.connect(); // Somehow it becomes connected here
            return;
        }
        LocationRequest request = LocationRequest.create();
        request.setInterval(LOCATION_UPDATES_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient.requestLocationUpdates(request, mLocationCallback);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(R.string.clear_map);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                clearMap();
                return true;
            }
        });
        return true;
    }

    public void clearMap() {
        mMap.clear();
        mLastLocation = null;
    }

    private class LocationCallback implements ConnectionCallbacks, OnConnectionFailedListener,
            LocationListener {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.v(LocationActivity.TAG, "Location Client Connected");

            // Display last location
            Location location = mLocationClient.getLastLocation();
            if (location != null) {
                handleLocation(location);
            }

            // Request for location updates
            LocationRequest request = LocationRequest.create();
            request.setInterval(LOCATION_UPDATES_INTERVAL);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationClient.requestLocationUpdates(request, mLocationCallback);

            // Setup map to allow adding Geo Fences
        }

        @Override
        public void onDisconnected() {
            Log.v(LocationActivity.TAG, "Location Client disconnected by the system");
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.v(LocationActivity.TAG, "Location Client connection failed");
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                Log.v(LocationActivity.TAG, "onLocationChanged: location == null");
                return;
            }
            // Add a marker iff location has changed.
            if (mLastLocation != null &&
                    mLastLocation.getLatitude() == location.getLatitude() &&
                    mLastLocation.getLongitude() == location.getLongitude()) {
                return;
            }

            handleLocation(location);
        }

        private void handleLocation(Location location) {
            // Update the mLocationStatus with the lat/lng of the location
            Log.v(LocationActivity.TAG, "LocationChanged == @" +
                    location.getLatitude() + "," + location.getLongitude());
            mLocationStatus.setText("Location changed @" + location.getLatitude() + "," +
                    location.getLongitude());

            // Add a marker of that location to the map
            LatLng latlongzoom = new LatLng(location.getLatitude(),
                    location.getLongitude());
            String snippet = location.getLatitude() + "," + location.getLongitude();
            Marker marker = mMap.addMarker(
                    new MarkerOptions().position(latlongzoom));
            marker.setSnippet(snippet);
            marker.setTitle(snippet);

            // Center the map to the first marker
            if (mLastLocation == null) {
                mMap.moveCamera(CameraUpdateFactory.
                        newCameraPosition(CameraPosition.fromLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()),
                                (float) 16.0)));
            }
            mLastLocation = location;
        }

    };

    private class ActivityRecognitionCallback implements ConnectionCallbacks,
            OnConnectionFailedListener {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.v(LocationActivity.TAG, "Activity Recognition Client connected");

            // Request activity updates
            Intent intent = new Intent(LocationActivity.this,
                    ActivityRecognitionIntentService.class);
            intent.setAction(LocationActivity.ACTION_ACTIVITY_RECOGNITION);
            mActivityRecognitionPendingIntent = PendingIntent.getService(LocationActivity.this, 0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mActivityRecognitionClient.requestActivityUpdates(ACTIVITY_UPDATES_INTERVAL,
                    mActivityRecognitionPendingIntent);
        }

        @Override
        public void onDisconnected() {
            Log.v(LocationActivity.TAG, "Activity Recognition Client disconnected by the system");
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.v(LocationActivity.TAG,
                    "Activity Recognition Client connection failed " + result.getErrorCode());
        }
    };

    public void startActivityDetection(View v) {
        if (!mActivityRecognitionClient.isConnected()) {
            mActivityRecognitionClient.connect();
        }
    }

    public void stopActivityDetection(View v) {
        if (mActivityRecognitionClient.isConnected()) {
            mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
            mActivityRecognitionClient.disconnect();
        }
    }

    private class GeoFenceCallback implements OnMapLongClickListener,
            OnAddGeofencesResultListener {

        @Override
        public void onMapLongClick(LatLng point) {
            Log.v(LocationActivity.TAG,
                    "onMapLongClick == " + point.latitude + "," + point.longitude);
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(point).radius(GEOFENCE_RADIUS).strokeColor(
                    android.graphics.Color.BLUE).strokeWidth(2);
            Circle circle = mMap.addCircle(circleOptions);
            String key = Integer.toString(id);
            id++;
            mGeoFences.put(key, circle);
            addGeoFences();
        }

        // Creates Geofence objects from all circles on the map and calls
        // addGeofences API.
        private void addGeoFences() {
            List<Geofence> list = new ArrayList<Geofence>();
            for (Map.Entry<String, Circle> entry : mGeoFences.entrySet()) {
                Circle circle = entry.getValue();
                Log.v(LocationActivity.TAG, "points == " +
                        circle.getCenter().latitude + "," +
                        circle.getCenter().longitude);
                Geofence geofence = new Geofence.Builder()
                        .setRequestId(entry.getKey())
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setCircularRegion(circle.getCenter().latitude,
                                circle.getCenter().longitude,
                                (float) circle.getRadius())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE).build();
                list.add(geofence);
            }
            if (list.isEmpty()) {
                return;
            }
            // Clear off all the currently triggering geo_fences before new
            // fences
            // are added.
            for (Circle triggeringGeoFence : mTriggeringFences.values()) {
                triggeringGeoFence.remove();
            }
            mTriggeringFences.clear();
            Log.v(LocationActivity.TAG, "addingGeoFences size = " + list.size());
            mLocationClient.addGeofences(list, getPendingIntent(), this);
        }

        private PendingIntent getPendingIntent() {
            Intent intent = new Intent(ACTION_GEOFENCE);
            intent.setComponent(new ComponentName(LocationActivity.this,
                    GeoFenceIntentReceiver.class));
            return PendingIntent.getBroadcast(LocationActivity.this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @Override
        public void onAddGeofencesResult(int statusCode,
                String[] geofenceRequestIds) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < geofenceRequestIds.length - 1; ++i) {
                builder.append(geofenceRequestIds[i]);
                builder.append(",");
            }
            builder.append(geofenceRequestIds[geofenceRequestIds.length - 1]);
            Log.v(LocationActivity.TAG, "Added Geofences == "
                    + statusCodeToString(statusCode) + " " + builder.toString());
            mGeoFenceStatus.setText("Added Geofences "
                    + statusCodeToString(statusCode) + " " + builder.toString());
        }

        private String statusCodeToString(int statusCode) {
            switch (statusCode) {
                case LocationStatusCodes.SUCCESS:
                    return "SUCCESS";
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case LocationStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case LocationStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
                case LocationStatusCodes.ERROR:
                    return "ERROR";
            }
            return "UNKNOWN";
        }
    }

    // Triggered when startAcitivity method is called in GeoFenceIntentReceiver.
    // Updates UI as geofences are entered/exited.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
        boolean receiverStarted =
                intent.getBooleanExtra("RECEIVER_STARTED", false);
        if (!receiverStarted) {
            return;
        }
        Bundle bundle = intent.getParcelableExtra("geo_fences");
        ArrayList<String> requestIds =
                bundle.getStringArrayList("request_ids");
        if (requestIds == null) {
            Log.v(LocationActivity.TAG, "request_ids == null");
            return;
        }
        int transition = intent.getIntExtra("transition", -2);

        for (String requestId : requestIds) {
            Log.v(LocationActivity.TAG, "Triggering Geo Fence requestId "
                    + requestId);
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Circle circle = mGeoFences.get(requestId);
                if (circle == null) {
                    continue;
                }
                Log.v(LocationActivity.TAG, "triggering_geo_fences enter == "
                        + requestId);

                // Add a superimposed red circle when a geofence is entered and
                // put the corresponding object in triggering_fences.
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(circle.getCenter())
                        .radius(circle.getRadius())
                        .fillColor(Color.argb(100, 100, 0, 0));
                Circle newCircle = mMap.addCircle(circleOptions);
                mTriggeringFences.put(requestId, newCircle);
            } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.v(LocationActivity.TAG, "triggering_geo_fences exit == "
                        + requestId);
                Circle circle = mTriggeringFences.get(requestId);
                if (circle == null) {
                    continue;
                }
                // Remove the superimposed red circle from the map and the
                // corresponding Circle object from triggering_fences hash_map.
                circle.remove();
                mTriggeringFences.remove(requestId);
            }
        }
        return;
    }

}
