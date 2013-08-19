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

package com.android.google.codelab.location;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class LocationActivity extends FragmentActivity {
	public static String TAG = "LocationActivity";
	public static boolean isAppForeground = false;
	private static final int ERROR_DIALOG_ON_CREATE_REQUEST_CODE = 4055;
	private static final int ERROR_DIALOG_ON_RESUME_REQUEST_CODE = 4056;

	// Shared variables
	private GoogleMap mMap;
	private Dialog errorDialog;
	
	// Location Request variables
	
	// Activity Recognition variables
	
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
		
		// Initialize Action Recognition
		
		// Initialize Geo Fencing
	}

	@Override
	public void onPause() {
		super.onPause();

		// Indicate the application is in background
		isAppForeground = false;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Indicate the application is in foreground
		isAppForeground = true;

		checkGooglePlayServiceAvailability(ERROR_DIALOG_ON_RESUME_REQUEST_CODE);
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
				break;
			}
		}
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
	}

}
