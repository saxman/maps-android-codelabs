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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * The about dialog displaying license information.
 */
public class AboutDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about_title);

        // Inflate the layout and get views
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_about,null);
        TextView text = (TextView) v.findViewById(R.id.about_text);
        TextView playText = (TextView) v.findViewById(R.id.about_text_play);

        // Generate and set text
        final Resources res = getResources();
        StringBuffer buffer = new StringBuffer();
        buffer.append(res.getString(R.string.about_icons));
        buffer.append(res.getString(R.string.about_playservices));
        text.setText(Html.fromHtml(buffer.toString()));

        // Set the Play Services license string
        playText.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity()));

        // set the view as dialog content
        builder.setView(v);

        return builder.create();
    }

}
