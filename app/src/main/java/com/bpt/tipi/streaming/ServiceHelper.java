package com.bpt.tipi.streaming;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.mqtt.MqttService;
import com.bpt.tipi.streaming.service.CheckPeriodicalService;
import com.bpt.tipi.streaming.service.LocationService;
import com.bpt.tipi.streaming.service.RecorderService;

/**
 * Created by jpujolji on 12/03/18.
 */

public class ServiceHelper {

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startAllServices(Context context) {
        startLocationService(context);
        startMqttService(context);
        startRecorderService(context);
    }

    public static void stopAllServices(Context context) {
        stopLocationService(context);
        stopMqttService(context);
        stopRecorderService(context);
    }

    public static synchronized void startLocationService(Context context) {

        if (!Utils.isServiceRunning(context, LocationService.class)) {
            context.startService(new Intent(context, LocationService.class));
            Toast.makeText(context, ".:startLocationService:.", Toast.LENGTH_SHORT).show();
        }
    }

    public static synchronized void stopLocationService(Context context) {
        if (Utils.isServiceRunning(context, LocationService.class)) {
            context.stopService(new Intent(context, LocationService.class));
        }
    }

    public static synchronized void stopMqttService(Context context) {
        if (Utils.isServiceRunning(context, MqttService.class)) {
            context.stopService(new Intent(context, MqttService.class));
        }
    }

    public static synchronized void startMqttService(Context context) {
        String idDevice = PreferencesHelper.getDeviceId(context);

        if (!idDevice.isEmpty()) {
            if (!Utils.isServiceRunning(context, MqttService.class)) {
                context.startService(new Intent(context, MqttService.class));
                Toast.makeText(context, ".:startMqttService:.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static synchronized void stopRecorderService(Context context) {
        if (Utils.isServiceRunning(context, RecorderService.class)) {
            context.stopService(new Intent(context, RecorderService.class));
        }
    }

    public static synchronized void startRecorderService(Context context) {
        if (!Utils.isServiceRunning(context, RecorderService.class)) {
            context.startService(new Intent(context, RecorderService.class));
            Toast.makeText(context, ".:startRecorderService:.", Toast.LENGTH_SHORT).show();
        }
    }

    public static synchronized void startCheckPeriodicalService(Context context) {
        if (!Utils.isServiceRunning(context, CheckPeriodicalService.class)) {
            context.startService(new Intent(context, CheckPeriodicalService.class));
        }
    }
}
