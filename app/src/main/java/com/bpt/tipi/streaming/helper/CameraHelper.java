package com.bpt.tipi.streaming.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;
import com.bpt.tipi.streaming.service.RecorderService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P;

public class CameraHelper {

    public static int getStreamingImageWidth(Context context) {
        int videoSize = PreferencesHelper.getStreamingVideoSize(context);
        switch (videoSize) {
            case 0:
            case 1:
            case 2:
                return 320;
            case 3:
            case 4:
            case 5:
                return 352;
            case 6:
            case 7:
            case 8:
                return 480;
            case 9:
            case 10:
            case 11:
                return 640;
            case 12:
            case 13:
            case 14:
                return 800;
            case 15:
            case 16:
            case 17:
                return 1280;
            default:
                return 0;
        }
    }


    public static int getStreamingImageHeight(Context context) {
        int videoSize = PreferencesHelper.getStreamingVideoSize(context);
        switch (videoSize) {
            case 0:
            case 1:
            case 2:
                return 240;
            case 3:
            case 4:
            case 5:
                return 288;
            case 6:
            case 7:
            case 8:
                return 320;
            case 9:
            case 10:
            case 11:
                return 480;
            case 12:
            case 13:
            case 14:
                return 600;
            case 15:
            case 16:
            case 17:
                return 720;
            default:
                return 0;
        }
    }

    private static int getStreamingVideoBitrate(Context context) {
        int videoSize = PreferencesHelper.getStreamingVideoSize(context);
        switch (videoSize) {
            case 0:
            case 3:
                return 140;
            case 1:
            case 4:
            case 6:
                return 280;
            case 2:
            case 5:
            case 7:
                return 560;
            case 8:
            case 9:
                return 700;
            case 10:
            case 12:
            case 15:
                return 1000;
            case 11:
            case 13:
                return 1500;
            case 14:
                return 2000;
            case 16:
                return 2500;
            case 17:
                return 3750;
            default:
                return 0;
        }
    }

    public static int getStreamingFramerate(Context context) {
        int select = PreferencesHelper.getStreamingFramerate(context);
        String[] framerates = context.getResources().getStringArray(R.array.streaming_framerates);
        return Integer.parseInt(framerates[select]);
    }

    public static int getLocalImageWidth(Context context) {
        int videoSize = PreferencesHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 0:
            case 1:
            case 2:
                return 1920;
            case 3:
            case 4:
            case 5:
                return 1280;
            case 6:
            case 7:
            case 8:
                return 640;
            default:
                return 0;
        }
    }

    public static int getLocalImageHeight(Context context) {
        int videoSize = PreferencesHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 0:
            case 1:
            case 2:
                return 1080;
            case 3:
            case 4:
            case 5:
                return 720;
            case 6:
            case 7:
            case 8:
                return 480;
            default:
                return 0;
        }
    }

    /*public static int getLocalVideoBitrate(Context context) {
        int videoSize = PreferencesHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 0:
            case 1:
            case 2:
                return 4000;
            case 3:
            case 4:
            case 5:
                return 2500;
            case 6:
            case 7:
            case 8:
                return 1500;
            default:
                return 0;
        }
    }*/

    public static int getLocalVideoBitrate(Context context) {
        int videoSize = PreferencesHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 0:
            case 1:
            case 2:
                return 5200;
            case 3:
            case 4:
            case 5:
                return 2200;
            case 6:
            case 7:
            case 8:
                return 1500;
            default:
                return 0;
        }
    }

    public static int getLocalFramerate(Context context) {
        int select = PreferencesHelper.getLocalFramerate(context);
        String[] framerates = context.getResources().getStringArray(R.array.local_video_framerates);
        return Integer.parseInt(framerates[select]);
    }

    public static FFmpegFrameRecorder initStreamingRecorder(Context context) {
        FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(CameraHelper.buildStreamEndpoint(context), getStreamingImageWidth(context), getStreamingImageHeight(context), 1);
        int fps = CameraHelper.getStreamingFramerate(context);
        frameRecorder.setFrameRate(fps);
        frameRecorder.setVideoBitrate(getStreamingVideoBitrate(context));

        frameRecorder.setFormat("flv");
        frameRecorder.setInterleaved(true);
        frameRecorder.setVideoOption("preset", "ultrafast");
        frameRecorder.setVideoOption("tune", "zerolatency");
        frameRecorder.setVideoOption("fflags", "nobuffer");
        frameRecorder.setVideoOption("analyzeduration", "0");
        frameRecorder.setVideoOption("crf", "28");
        frameRecorder.setGopSize(30);
        frameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        frameRecorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        frameRecorder.setAudioBitrate(RecorderService.AUDIO_BITRATE);
        frameRecorder.setAudioOption("crf", "0");
        frameRecorder.setAudioQuality(0);
        frameRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        frameRecorder.setSampleRate(RecorderService.AUDIO_RATE_IN_HZ);

        return frameRecorder;
    }

    public static String buildStreamEndpoint(Context context) {
        String wowzaIp = PreferencesHelper.getUrlStreaming(context);
        String portNumber = "" + PreferencesHelper.getPortStreaming(context);
        String appName = PreferencesHelper.getAppNameStreaming(context);
        String streamName = PreferencesHelper.getDeviceId(context);
        String username = PreferencesHelper.getUsernameStreaming(context);
        String password = PreferencesHelper.getPasswordStreaming(context);

        StringBuilder builder = new StringBuilder();
        builder.append("rtmp://");
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            builder.append(username).append(":").append(password).append("@");
        }
        builder.append(wowzaIp).append(":").append(portNumber).append("/");
        builder.append(appName).append("/");
        builder.append(streamName);
        return builder.toString();
    }

    public static void sendSignalSOS(final Context context) {
        HttpClient httpClient = new HttpClient(context, new HttpInterface() {
            @Override
            public void onSuccess(String method, JSONObject response) {

            }

            @Override
            public void onFailed(String method, JSONObject errorResponse) {

            }
        });
        String method = HttpHelper.Method.SOS + "/" + PreferencesHelper.getDeviceId(context);
        httpClient.httpRequest("", method, HttpHelper.TypeRequest.TYPE_PUT, false);
    }

    public static void sendLogStreaming(Context context, long bytesTransmited, Date startDate) {
        HttpClient httpClient = new HttpClient(context, new HttpInterface() {
            @Override
            public void onSuccess(String method, JSONObject response) {

            }

            @Override
            public void onFailed(String method, JSONObject errorResponse) {

            }
        });

        JSONObject json = new JSONObject();
        try {
            DateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            json.put("deviceName", PreferencesHelper.getDeviceId(context));
            json.put("startStr", dt.format(startDate));
            json.put("bytesTransmited", bytesTransmited);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpClient.httpRequest(json.toString(), HttpHelper.Method.LOG_STREAMING, HttpHelper.TypeRequest.TYPE_POST, false);
    }

    public static void soundStart(Context context) {
        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.audio_start);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mPlayer.start();
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(800);
    }

    public static void soundStop(Context context) {
        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.audio_stop);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mPlayer.start();
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(800);
    }

    public static void soundTakePhoto(Context context) {
        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.audio_take_photo);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mPlayer.start();
    }
}
