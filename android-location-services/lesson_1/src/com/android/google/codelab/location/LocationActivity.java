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

package com.android.google.codelab.location;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
	private final int LOCATION_UPDATES_INTERVAL = 10000; // Setting 10 sec interval for location updates
	
	// Geo Fencing variables

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
		
		restartLocationClient();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
			Log.v(LocationActivity.TAG, "Location Client connected");
			
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

}
