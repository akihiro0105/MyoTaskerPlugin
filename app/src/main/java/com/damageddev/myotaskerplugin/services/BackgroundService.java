/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.damageddev.myotaskerplugin.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.damageddev.myotaskerplugin.EditActivity;
import com.damageddev.myotaskerplugin.utils.Constants;
import com.damageddev.myotaskerplugin.utils.TaskerPlugin;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;


public final class BackgroundService extends Service {
    protected static final Intent INTENT_REQUEST_REQUERY =
            new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY)
                    .putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
                            EditActivity.class.getName());


    private BroadcastReceiver mReceiver;
    private Toast mToast;


    @Override
    public void onCreate() {
        super.onCreate();
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            stopSelf();
            return;
        }
        hub.addListener(mListener);
        hub.pairWithAnyMyo();
    }

    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            showToast("My connected.");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.POSE, pose.toString());

            TaskerPlugin.Event.addPassThroughData(INTENT_REQUEST_REQUERY, bundle);
            BackgroundService.this.sendBroadcast(INTENT_REQUEST_REQUERY);
            showToast(pose.toString());
        }
    };

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }

        mToast.show();
    }

}