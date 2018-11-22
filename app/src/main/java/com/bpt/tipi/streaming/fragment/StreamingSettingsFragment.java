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
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.activity.SettingsActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class StreamingSettingsFragment extends Fragment implements View.OnClickListener {

    LinearLayout linearVideoSize, linearFramerate, linearUrlServer, linearPortServer, linearNameServer, linearUsernameServer, linearPasswordServer;
    RelativeLayout linearVibrateSound;
    Switch swVibrateSound;

    public static StreamingSettingsFragment newInstance() {
        return new StreamingSettingsFragment();
    }

    public StreamingSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streaming_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        linearVideoSize = view.findViewById(R.id.linearVideoSize);
        linearFramerate = view.findViewById(R.id.linearFramerate);
        linearVibrateSound = view.findViewById(R.id.linearVibrateSound);
        swVibrateSound = view.findViewById(R.id.swVibrateSound);
        linearUrlServer = view.findViewById(R.id.linearUrlServer);
        linearPortServer = view.findViewById(R.id.linearPortServer);
        linearNameServer = view.findViewById(R.id.linearNameServer);
        linearUsernameServer = view.findViewById(R.id.linearUsernameServer);
        linearPasswordServer = view.findViewById(R.id.linearPasswordServer);

        linearVideoSize.setOnClickListener(this);
        linearFramerate.setOnClickListener(this);
        linearVibrateSound.setOnClickListener(this);
        linearUrlServer.setOnClickListener(this);
        linearPortServer.setOnClickListener(this);
        linearNameServer.setOnClickListener(this);
        linearUsernameServer.setOnClickListener(this);
        linearPasswordServer.setOnClickListener(this);

        boolean vibrateSound = PreferencesHelper.getStreamingVibrateAndSound(getContext());
        swVibrateSound.setChecked(vibrateSound);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearVideoSize:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSelectValueForSetting(R.string.title_streaming_video_size, R.string.key_streaming_video_size);
                break;
            case R.id.linearFramerate:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSelectValueForSetting(R.string.title_framerate, R.string.key_streaming_framerate);
                break;
            case R.id.linearVibrateSound:
                boolean vibrateSound = swVibrateSound.isChecked();
                swVibrateSound.setChecked(!vibrateSound);
                PreferencesHelper.setStreamingVibrateAndSound(getContext(), !vibrateSound);
                ((SettingsActivity)Objects.requireNonNull(getActivity())).sendConfig();
                break;
            case R.id.linearUrlServer:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_url_streaming, R.string.key_url_streaming, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearPortServer:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_port_streaming, R.string.key_port_streaming, EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case R.id.linearNameServer:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_app_name_streaming, R.string.key_app_name_streaming, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearUsernameServer:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_username_streaming, R.string.key_username_streaming, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearPasswordServer:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_password_streaming, R.string.key_password_streaming, EditorInfo.TYPE_CLASS_TEXT);
                break;
        }
    }
}