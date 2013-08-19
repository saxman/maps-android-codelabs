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

package com.example.android.touroflondon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {

    /**
     * Preference key used to indicate whether data has been loaded into the database.
     */
    private static final String PREFERENCE_DATA_LOADED = "PREFERENCE_DATA_LOADED";

    /**
     * Reads the route.json and parses it into a JSONObject.
     *
     * @param context
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject readRoute(Context context) throws IOException, JSONException {

        // Read file into String
        InputStream is = context.getResources().openRawResource(R.raw.tour);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            char[] buffer = new char[1024];
            int length;
            while ((length = br.read(buffer)) > 0) {
                sb.append(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }

        // Covert String to json
        JSONObject json = new JSONObject(sb.toString());

        return json;
    }

    /**
     * Mark the data as loaded.
     * This flag should be set to true once the route.json file has been parsed and stored in the database.
     *
     * @param context
     * @param hasData True if the 'data loaded' flag has been set.
     */
    public static void setDataLoaded(Context context, boolean hasData) {
        Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREFERENCE_DATA_LOADED, hasData);
        pref.commit();
    }

    /**
     * Returns true if route data has been loaded into the database.
     *
     * @param context
     * @return
     */
    public static boolean hasDataLoaded(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREFERENCE_DATA_LOADED, false);
    }
}
