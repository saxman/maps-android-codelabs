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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * ActivityRecognitionIntentService receives activity updates and
 * broadcasts them to the ActivityRecognitionIntentReceiver.
 */
public class ActivityRecognitionIntentService extends IntentService {
	public ActivityRecognitionIntentService() {
		super("ActivityRecognitionIntentService");
	}

	public ActivityRecognitionIntentService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction() != LocationActivity.ACTION_ACTIVITY_RECOGNITION) {
			return;
		}
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult
					.extractResult(intent);
			DetectedActivity detectedActivity = result
					.getMostProbableActivity();
			int activityType = detectedActivity.getType();

			Log.v(LocationActivity.TAG, "activity_type == " + activityType);

			// Put the activity_type as an intent extra and send a broadcast.
			Intent send_intent = new Intent(
					LocationActivity.ACTION_ACTIVITY_RECOGNITION);
			send_intent.putExtra("activity_type", activityType);
			sendBroadcast(send_intent);
		}
	}
}
