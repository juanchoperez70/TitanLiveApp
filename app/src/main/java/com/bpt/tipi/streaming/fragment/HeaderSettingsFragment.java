package com.bpt.tipi.streaming.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.activity.SettingsActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HeaderSettingsFragment extends Fragment implements View.OnClickListener {

    TextView tvGeneralSettings, tvConnectionSettings, tvVideoSettings, tvStreamingSettings, tvDeviceSettings;

    public static HeaderSettingsFragment newInstance() {
        return new HeaderSettingsFragment();
    }

    public HeaderSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvGeneralSettings = view.findViewById(R.id.tvGeneralSettings);
        tvConnectionSettings = view.findViewById(R.id.tvConnectionSettings);
        tvVideoSettings = view.findViewById(R.id.tvVideoSettings);
        tvStreamingSettings = view.findViewById(R.id.tvStreamingSettings);
        tvDeviceSettings = view.findViewById(R.id.tvDeviceSettings);

        tvGeneralSettings.setOnClickListener(this);
        tvConnectionSettings.setOnClickListener(this);
        tvVideoSettings.setOnClickListener(this);
        tvStreamingSettings.setOnClickListener(this);
        tvDeviceSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvGeneralSettings:
                ((SettingsActivity) Objects.requireNonNull(getActivity())).onSelectContent(SettingsActivity.CODE_GENERAL_FRAGMENT);
                break;
            case R.id.tvConnectionSettings:
                ((SettingsActivity) Objects.requireNonNull(getActivity())).onSelectContent(SettingsActivity.CODE_CONNECTION_FRAGMENT);
                break;
            case R.id.tvVideoSettings:
                ((SettingsActivity) Objects.requireNonNull(getActivity())).onSelectContent(SettingsActivity.CODE_VIDEO_FRAGMENT);
                break;
            case R.id.tvStreamingSettings:
                ((SettingsActivity) Objects.requireNonNull(getActivity())).onSelectContent(SettingsActivity.CODE_STREAMING_FRAGMENT);
                break;
            case R.id.tvDeviceSettings:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
        }
    }
}
