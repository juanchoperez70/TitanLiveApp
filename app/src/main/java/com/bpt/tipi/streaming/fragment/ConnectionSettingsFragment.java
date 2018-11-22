package com.bpt.tipi.streaming.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.activity.SettingsActivity;
import com.bpt.tipi.streaming.helper.PreferencesHelper;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionSettingsFragment extends Fragment implements View.OnClickListener {

    LinearLayout linearUrlApi, linearUrlTitan, linearUrlMqtt,
            linearPortMqtt, linearUsernameMqtt, linearPasswordMqtt;

    public static ConnectionSettingsFragment newInstance() {
        return new ConnectionSettingsFragment();
    }

    public ConnectionSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        linearUrlApi = view.findViewById(R.id.linearUrlApi);
        linearUrlTitan = view.findViewById(R.id.linearUrlTitan);
        linearUrlMqtt = view.findViewById(R.id.linearUrlMqtt);
        linearPortMqtt = view.findViewById(R.id.linearPortMqtt);
        linearUsernameMqtt = view.findViewById(R.id.linearUsernameMqtt);
        linearPasswordMqtt = view.findViewById(R.id.linearPasswordMqtt);

        linearUrlApi.setOnClickListener(this);
        linearUrlTitan.setOnClickListener(this);
        linearUrlMqtt.setOnClickListener(this);
        linearPortMqtt.setOnClickListener(this);
        linearUsernameMqtt.setOnClickListener(this);
        linearPasswordMqtt.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearUrlApi:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_url_api, R.string.key_url_api, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearUrlTitan:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_url_titan, R.string.key_url_titan, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearUrlMqtt:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_url_mqtt, R.string.key_url_mqtt, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearPortMqtt:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_port_mqtt, R.string.key_port_mqtt, EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case R.id.linearUsernameMqtt:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_username_mqtt, R.string.key_username_mqtt, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearPasswordMqtt:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_password_mqtt, R.string.key_password_mqtt, EditorInfo.TYPE_CLASS_TEXT);
                break;
        }
    }
}
