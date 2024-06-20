/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile.core.testapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import androidx.core.os.BuildCompat;
import androidx.core.os.UserManagerCompat;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Received action: " + action + ", isUserUnlocked(): " + UserManagerCompat
                .isUserUnlocked(context));
    }
}