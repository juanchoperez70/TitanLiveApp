package com.bpt.tipi.streaming.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.UnCaughtException;
import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.fragment.ConnectionSettingsFragment;
import com.bpt.tipi.streaming.fragment.GeneralSettingsFragment;
import com.bpt.tipi.streaming.fragment.HeaderSettingsFragment;
import com.bpt.tipi.streaming.fragment.StreamingSettingsFragment;
import com.bpt.tipi.streaming.fragment.VideoSettingsFragment;
import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.model.RemoteConfig;
import com.bpt.tipi.streaming.mqtt.MqttService;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;
import com.bpt.tipi.streaming.service.LocationService;
import com.bpt.tipi.streaming.service.RecorderService;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.spec.ECField;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements HttpInterface {

    public static final int CODE_HEADER_FRAGMENT = 0;
    public static final int CODE_GENERAL_FRAGMENT = 1;
    public static final int CODE_CONNECTION_FRAGMENT = 2;
    public static final int CODE_VIDEO_FRAGMENT = 3;
    public static final int CODE_STREAMING_FRAGMENT = 4;

    private int fragmentSelected = CODE_HEADER_FRAGMENT;

    private Context context;

    private EventBus bus = EventBus.getDefault();
    private String device = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = SettingsActivity.this;

        onSelectContent(CODE_HEADER_FRAGMENT);

    }

    public void onSelectContent(int code) {
        fragmentSelected = code;
        switch (code) {
            case CODE_HEADER_FRAGMENT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, HeaderSettingsFragment.newInstance()).commit();
                break;
            case CODE_GENERAL_FRAGMENT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, GeneralSettingsFragment.newInstance()).commit();
                break;
            case CODE_CONNECTION_FRAGMENT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ConnectionSettingsFragment.newInstance()).commit();
                break;
            case CODE_VIDEO_FRAGMENT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, VideoSettingsFragment.newInstance()).commit();
                break;
            case CODE_STREAMING_FRAGMENT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, StreamingSettingsFragment.newInstance()).commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentSelected != CODE_HEADER_FRAGMENT) {
            fragmentSelected = CODE_HEADER_FRAGMENT;
            onSelectContent(CODE_HEADER_FRAGMENT);
        } else {
            super.onBackPressed();
        }
    }

    public void showAlertSetValueForSetting(int title, final int key, int inputType) {
        final Dialog dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.enter_value_settings_dialog);
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        final EditText etValue = dialog.findViewById(R.id.etValue);
        Button btnAccept = dialog.findViewById(R.id.btnAccept);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        etValue.setInputType(inputType);

        tvTitle.setText(getString(title));
        etValue.setText(getValue(key));

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etValue.getText().toString().trim().isEmpty()) {
                    setValue(key, etValue.getText().toString().trim());
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Ingrese un valor válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showAlertSelectValueForSetting(int title, final int key) {
        final Dialog dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.select_value_settings_dialog);
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        final Spinner spOptions = dialog.findViewById(R.id.spOptions);
        Button btnAccept = dialog.findViewById(R.id.btnAccept);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        tvTitle.setText(getString(title));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this, R.layout.item_list_spinner, getListOptions(key));
        spOptions.setAdapter(spinnerArrayAdapter);
        spOptions.setSelection(Integer.parseInt(getValue(key)));

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int option = spOptions.getSelectedItemPosition();
                setValue(key, "" + option);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public String getValue(int key) {
        switch (key) {
            case R.string.key_device_id:
                return PreferencesHelper.getDeviceId(context);
            case R.string.key_password_for_settings:
                return PreferencesHelper.getPasswordForSettings(context);
            case R.string.key_interval_location:
                return "" + PreferencesHelper.getIntervalLocation(context);
            case R.string.key_interval_location_sos:
                return "" + PreferencesHelper.getIntervalLocationInSos(context);
            case R.string.key_url_api:
                return "" + PreferencesHelper.getUrlApi(context);
            case R.string.key_url_titan:
                return "" + PreferencesHelper.getUrlTitan(context);
            case R.string.key_url_mqtt:
                return "" + PreferencesHelper.getURLMqtt(context);
            case R.string.key_port_mqtt:
                return "" + PreferencesHelper.getPortMqtt(context);
            case R.string.key_username_mqtt:
                return "" + PreferencesHelper.getUsernameMqtt(context);
            case R.string.key_password_mqtt:
                return "" + PreferencesHelper.getPasswordMqtt(context);
            case R.string.key_local_video_size:
                return "" + PreferencesHelper.getLocalVideoSize(context);
            case R.string.key_local_framerate:
                return "" + PreferencesHelper.getLocalFramerate(context);
            case R.string.key_local_video_duration:
                return "" + PreferencesHelper.getLocalVideoDuration(context);
            case R.string.key_post_video_duration:
                return "" + PreferencesHelper.getPostVideoDuration(context);
            case R.string.key_streaming_video_size:
                return "" + PreferencesHelper.getStreamingVideoSize(context);
            case R.string.key_streaming_framerate:
                return "" + PreferencesHelper.getStreamingFramerate(context);
            case R.string.key_url_streaming:
                return "" + PreferencesHelper.getUrlStreaming(context);
            case R.string.key_port_streaming:
                return "" + PreferencesHelper.getPortStreaming(context);
            case R.string.key_app_name_streaming:
                return "" + PreferencesHelper.getAppNameStreaming(context);
            case R.string.key_username_streaming:
                return "" + PreferencesHelper.getUsernameStreaming(context);
            case R.string.key_password_streaming:
                return "" + PreferencesHelper.getPasswordStreaming(context);
            default:
                return "";
        }
    }

    public String[] getListOptions(int key) {
        switch (key) {
            case R.string.key_local_video_size:
                return getResources().getStringArray(R.array.local_video_sizes);
            case R.string.key_local_framerate:
                return getResources().getStringArray(R.array.local_video_framerates);
            case R.string.key_streaming_video_size:
                return getResources().getStringArray(R.array.streaming_video_sizes);
            case R.string.key_streaming_framerate:
                return getResources().getStringArray(R.array.streaming_framerates);
            default:
                return null;
        }
    }

    public void setValue(int key, String value) {
        MessageEvent event;
        switch (key) {
            case R.string.key_device_id:
                device = value;
                registerDevice(value);
                break;
            case R.string.key_password_for_settings:
                PreferencesHelper.setPasswordForSettings(context, value);
                sendConfig();
                break;
            case R.string.key_interval_location:
                PreferencesHelper.setIntervalLocation(context, value);
                event = new MessageEvent(MessageEvent.LOCATION_PARAMETERS_CONFIGURED);
                bus.post(event);
                sendConfig();
                break;
            case R.string.key_interval_location_sos:
                PreferencesHelper.setIntervalLocationInSos(context, value);
                sendConfig();
                break;
            case R.string.key_url_api:
                PreferencesHelper.setUrlApi(context, value);
                sendConfig();
                break;
            case R.string.key_url_titan:
                PreferencesHelper.setUrlTitan(context, value);
                sendConfig();
                break;
            case R.string.key_url_mqtt:
                PreferencesHelper.setUrlMqtt(context, value);
                event = new MessageEvent(MessageEvent.MQTT_PARAMETERS_CONFIGURED);
                bus.post(event);
                sendConfig();
                break;
            case R.string.key_port_mqtt:
                PreferencesHelper.setPortMqtt(context, value);
                event = new MessageEvent(MessageEvent.MQTT_PARAMETERS_CONFIGURED);
                bus.post(event);
                sendConfig();
                break;
            case R.string.key_username_mqtt:
                PreferencesHelper.setUsernameMqtt(context, value);
                event = new MessageEvent(MessageEvent.MQTT_PARAMETERS_CONFIGURED);
                bus.post(event);
                sendConfig();
                break;
            case R.string.key_password_mqtt:
                PreferencesHelper.setPasswordMqtt(context, value);
                event = new MessageEvent(MessageEvent.MQTT_PARAMETERS_CONFIGURED);
                bus.post(event);
                sendConfig();
                break;
            case R.string.key_local_video_size:
                PreferencesHelper.setLocalVideoSize(context, value);
                sendConfig();
                break;
            case R.string.key_local_framerate:
                PreferencesHelper.setLocalFramerate(context, value);
                sendConfig();
                break;
            case R.string.key_local_video_duration:
                PreferencesHelper.setLocalVideoDuration(context, value);
                sendConfig();
                break;
            case R.string.key_post_video_duration:
                PreferencesHelper.setPostVideoDuration(context, value);
                sendConfig();
                break;
            case R.string.key_streaming_video_size:
                PreferencesHelper.setStreamingVideoSize(context, value);
                ServiceHelper.stopMqttService(context);
                ServiceHelper.stopRecorderService(context);
                ServiceHelper.startMqttService(context);
                ServiceHelper.startRecorderService(context);
                sendConfig();
                break;
            case R.string.key_streaming_framerate:
                PreferencesHelper.setStreamingFramerate(context, value);
                sendConfig();
                break;
            case R.string.key_url_streaming:
                PreferencesHelper.setUrlStreaming(context, value);
                sendConfig();
                break;
            case R.string.key_port_streaming:
                PreferencesHelper.setPortStreaming(context, value);
                sendConfig();
                break;
            case R.string.key_app_name_streaming:
                PreferencesHelper.setAppNameStreaming(context, value);
                sendConfig();
                break;
            case R.string.key_username_streaming:
                PreferencesHelper.setUsernameStreaming(context, value);
                sendConfig();
                break;
            case R.string.key_password_streaming:
                PreferencesHelper.setPasswordStreaming(context, value);
                sendConfig();
                break;
        }
    }

    public void registerDevice(String device) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imei", Utils.getImeiDevice(context));
            jsonObject.put("deviceName", device);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpClient httpClient = new HttpClient(context, this);
        httpClient.httpRequest(jsonObject.toString(), HttpHelper.Method.REGISTER_ID, HttpHelper.TypeRequest.TYPE_POST, true);
    }

    public void sendConfig() {
        RemoteConfig remoteConfig = PreferencesHelper.getConfig(context);
        Gson gson = new Gson();
        String json = gson.toJson(remoteConfig);
        HttpClient httpClient = new HttpClient(context, this);
        httpClient.httpRequest(json, HttpHelper.Method.SEND_CONFIG, HttpHelper.TypeRequest.TYPE_PUT, true);
    }

    @Override
    public void onSuccess(String method, JSONObject response) {
        if (method.equals(HttpHelper.Method.REGISTER_ID)) {
            Toast.makeText(context, response.optString("message"), Toast.LENGTH_LONG).show();
            if (response.optString("status").equals("1")) {
                PreferencesHelper.setDeviceId(context, device);

                if (!ServiceHelper.isServiceRunning(context, org.eclipse.paho.android.service.MqttService.class)) {
                    ServiceHelper.startMqttService(context);
                } else {
                    MessageEvent event = new MessageEvent(MessageEvent.ID_DEVICE_CONFIGURED);
                    bus.post(event);
                }
                if (!ServiceHelper.isServiceRunning(context, LocationService.class)) {
                    ServiceHelper.startLocationService(context);
                }
            }
        }
        if (method.equals(HttpHelper.Method.SEND_CONFIG)) {
            Toast.makeText(context, response.optString("message"), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailed(String method, JSONObject errorResponse) {
        if (method.equals(HttpHelper.Method.REGISTER_ID)) {
            Toast.makeText(context, "Ocurrió un error al registrar el ID, intente mas tarde", Toast.LENGTH_LONG).show();
        }
        if (method.equals(HttpHelper.Method.SEND_CONFIG)) {
            Toast.makeText(context, "Ocurrió un error al actualizar la configuración en el servidor, intente mas tarde", Toast.LENGTH_LONG).show();
        }
    }
}