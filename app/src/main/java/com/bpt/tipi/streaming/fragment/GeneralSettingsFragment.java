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

import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.activity.SettingsActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class GeneralSettingsFragment extends Fragment implements View.OnClickListener {

    LinearLayout linearIdDevice, linearPassword, linearLocation, linearLocationSOS;

    public static GeneralSettingsFragment newInstance() {
        return new GeneralSettingsFragment();
    }

    public GeneralSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_general_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        linearIdDevice = view.findViewById(R.id.linearIdDevice);
        linearPassword = view.findViewById(R.id.linearPassword);
        linearLocation = view.findViewById(R.id.linearLocation);
        linearLocationSOS = view.findViewById(R.id.linearLocationSOS);

        linearIdDevice.setOnClickListener(this);
        linearPassword.setOnClickListener(this);
        linearLocation.setOnClickListener(this);
        linearLocationSOS.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearIdDevice:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_device_id, R.string.key_device_id, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearPassword:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_password_for_settings, R.string.key_password_for_settings, EditorInfo.TYPE_CLASS_TEXT);
                break;
            case R.id.linearLocation:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_interval_location, R.string.key_interval_location, EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case R.id.linearLocationSOS:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_interval_location_sos, R.string.key_interval_location_sos, EditorInfo.TYPE_CLASS_NUMBER);
                break;
        }
    }

}
