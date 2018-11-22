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
import android.widget.Toast;

import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.activity.SettingsActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoSettingsFragment extends Fragment implements View.OnClickListener {

    LinearLayout linearVideoSize, linearFramerate, linearVideoDuration, linearPostDuration;
    RelativeLayout linearVibrateSound, linearPostRecord;

    Switch swVibrateSound, swPostRecord;

    public static VideoSettingsFragment newInstance() {
        return new VideoSettingsFragment();
    }

    public VideoSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        linearVideoSize = view.findViewById(R.id.linearVideoSize);
        linearFramerate = view.findViewById(R.id.linearFramerate);
        linearVideoDuration = view.findViewById(R.id.linearVideoDuration);
        linearVibrateSound = view.findViewById(R.id.linearVibrateSound);
        linearPostRecord = view.findViewById(R.id.linearPostRecord);
        linearPostDuration = view.findViewById(R.id.linearPostDuration);

        swVibrateSound = view.findViewById(R.id.swVibrateSound);
        swPostRecord = view.findViewById(R.id.swPostRecord);

        linearVideoSize.setOnClickListener(this);
        linearFramerate.setOnClickListener(this);
        linearVideoDuration.setOnClickListener(this);
        linearVibrateSound.setOnClickListener(this);
        linearPostRecord.setOnClickListener(this);
        linearPostDuration.setOnClickListener(this);

        boolean vibrateSound = PreferencesHelper.getLocalVibrateAndSound(getContext());
        swVibrateSound.setChecked(vibrateSound);

        boolean postRecord = PreferencesHelper.getLocalPostRecord(getContext());
        swPostRecord.setChecked(postRecord);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearVideoSize:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSelectValueForSetting(R.string.title_local_video_size, R.string.key_local_video_size);
                break;
            case R.id.linearFramerate:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSelectValueForSetting(R.string.title_framerate, R.string.key_local_framerate);
                break;
            case R.id.linearVideoDuration:
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .showAlertSetValueForSetting(R.string.title_local_video_duration, R.string.key_local_video_duration, EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case R.id.linearVibrateSound:
                boolean vibrateSound = swVibrateSound.isChecked();
                swVibrateSound.setChecked(!vibrateSound);
                PreferencesHelper.setLocalVibrateAndSound(getContext(), !vibrateSound);
                ((SettingsActivity) Objects.requireNonNull(getActivity())).sendConfig();
                break;
            case R.id.linearPostRecord:
                boolean postRecord = swPostRecord.isChecked();
                swPostRecord.setChecked(!postRecord);
                PreferencesHelper.setLocalPostRecord(getContext(), !postRecord);
                ((SettingsActivity) Objects.requireNonNull(getActivity())).sendConfig();
                break;
            case R.id.linearPostDuration:
                boolean postRecordEnabled = swPostRecord.isChecked();
                if (postRecordEnabled) {
                    ((SettingsActivity) Objects.requireNonNull(getActivity()))
                            .showAlertSetValueForSetting(R.string.title_post_video_duration, R.string.key_post_video_duration, EditorInfo.TYPE_CLASS_NUMBER);
                } else {
                    Toast.makeText(getContext(), "El post-grabado no está activado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}