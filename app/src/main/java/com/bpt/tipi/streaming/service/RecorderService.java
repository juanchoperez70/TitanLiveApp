package com.bpt.tipi.streaming.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.bpt.tipi.streaming.BitmapUtils;
import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.StateMachineHandler;
import com.bpt.tipi.streaming.UnCaughtException;

import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.helper.CameraHelper;
import com.bpt.tipi.streaming.helper.WatermarkHelper;
import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.helper.VideoNameHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.receiver.events.CameraEventHandler;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameRecorder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecorderService extends Service implements Camera.PreviewCallback {

    //ForceStrop
    public boolean isForceDestroy = false;

    //TAG para Log
    private static final String TAG = "RecorderService";

    //Nombre para el hilo del servicio
    public static final String THREAD_NAME = "RecorderService";

    public static final int AUDIO_BITRATE = 128000;
    public static final int AUDIO_RATE_IN_HZ = 44100;

    private final IBinder mBinder = new RecorderBinder();
    private Handler mHandler;

    private EventBus bus = EventBus.getDefault();
    public Context context;

    private String deviceId;

    //Camara a utilizar.
    private Camera camera = null;

    //Camara primaria.
    private Camera primaryCamera = null;

    private byte buffer[];
    private byte primaryBuffer[];

    int sequence = 1;

    /* Variables de streaming */
    private FFmpegFrameRecorder streamingRecorder;
    private Frame streamingYuvImage = null;
    private FFmpegFrameFilter streamingFilter;

    private AudioRecord streamingAudioRecord;
    private StreamingAudioRecordRunnable streamingAudioRecordRunnable;
    private Thread streamingAudioThread;
    /**
     *el streamingRunAudioThread antes iniciaba en true pero al tomar foto se quedaba el streaming corriendo forever
     */
    volatile boolean streamingRunAudioThread = false;
    long streamingStartTime = 0;
    public boolean isStreamingRecording = false;

    /* Variables de local */
    MediaRecorder mediaRecorder;
    CamcorderProfile profile;
    public boolean isLocalRecording = false;

    public boolean isSos = false;
    int videoDuration = 0;

    boolean flashOn = false;
    boolean proccesingStreming = false;

    CounterLocalVideo counterLocalVideo;

    CounterPostVideo counterPostVideo;

    CounterFlash counterFlash;

    long mStartTX;
    Date streamingStarted;

    boolean takePhoto = false;

    StateMachineHandler machineHandler;
    boolean sosPressed = false;

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(this));
        context = RecorderService.this;
        HandlerThread thread = new HandlerThread(THREAD_NAME);
        thread.start();
        mHandler = new Handler(thread.getLooper());
        Log.i(TAG, "RecorderService onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bus.register(this);
        machineHandler = new StateMachineHandler(RecorderService.this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        Log.i(TAG, "RecorderService onDestroy()");
        if(isForceDestroy) {
            isStreamingRecording = false;
            isForceDestroy = false;
            finishCameraDestroy();
        }
        else{
            //finishCamera();
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.lock();
                camera.release();
                camera = null;
            }
        }
        finishPrimaryCamera();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event) {
        Log.e("Depuracion", "onMessageEvent " + event.key);
        switch (event.key) {
            case MessageEvent.SOS_PRESSED:
                if (!isSos) {
                    if (!isLocalRecording) {
                        setLocalRecorderStateMachine();
                    }
                    sosPressed = true;
                    isSos = true;
                    sendSos();
                    //Se inicia SOS
                    CameraEventHandler.appendEventLog(context, "INICIO SOS");
                } else {
                    isSos = false;
                    setLocalRecorderStateMachine();
                    //Se detiene SOS
                    CameraEventHandler.appendEventLog(context, "FIN SOS");
                }
                break;
            case MessageEvent.LOCAL_RECORD:
                if (isSos) {
                    isSos = false;
                }
                setLocalRecorderStateMachine();
                //Evento de grabación
                if (isLocalRecording) {
                    //Se inicia grabación LOCAL
                    CameraEventHandler.appendEventLog(context, "INICIO GRABACION");
                } else {
                    //Se inicia grabación LOCAL
                    CameraEventHandler.appendEventLog(context, "FIN GRABACION");
                }
                break;
            case MessageEvent.START_STREAMING:
                if (!proccesingStreming) {
                    proccesingStreming = true;
                    if (!isStreamingRecording) {
                        machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
                        mStartTX = TrafficStats.getTotalTxBytes();
                        Calendar cal = Calendar.getInstance();
                        streamingStarted = cal.getTime();
                    } else {
                        proccesingStreming = false;
                    }
                }
                break;
            case MessageEvent.STOP_STREAMING:
                if (!proccesingStreming) {
                    proccesingStreming = true;
                    if (isStreamingRecording) {
                        machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
                        sendLogStreaming();
                    } else {
                        proccesingStreming = false;
                    }
                }
                break;
            case MessageEvent.TAKE_PHOTO:
                machineHandler.sendEmptyMessage(StateMachineHandler.TAKE_PHOTO);
                //Se toma fotografía
                CameraEventHandler.appendEventLog(context, "CAPTURA FOTO");
                break;
            case MessageEvent.STATE_FLASH:
                if (!flashOn) {
                    flashLightOn();
                } else {
                    pauseFlashCounter();
                    flashLightOff();
                }
                flashOn = !flashOn;
                break;
            case MessageEvent.FORCE_RECORDER_STOP:
                setForceDestroy();
                break;
        }
    }

    public void setLocalRecorderStateMachine() {
        Message message = new Message();
        message.what = StateMachineHandler.LOCAL_RECORDER_PRESSED;
        message.arg1 = StateMachineHandler.PLAY_SOUND;
        machineHandler.handleMessage(message);
    }

    /**
     * Se debe abrir siempre primero la cámara secundaria 0
     */
    public void prepareStreamingCamera() {
        if (camera == null) {
            //camera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);

            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

                Camera.Parameters parameters = camera.getParameters();

                parameters.setPreviewSize(CameraHelper.getStreamingImageWidth(context), CameraHelper.getStreamingImageHeight(context));
                parameters.setPreviewFrameRate(ConfigHelper.getStreamingFramerate(context));

                parameters.setPreviewFormat(ImageFormat.NV21);
                /*if(parameters.isAutoExposureLockSupported() && parameters.isAutoWhiteBalanceLockSupported()){
                    parameters.setAutoExposureLock(true);
                    parameters.setAutoWhiteBalanceLock(true);
                }*/
                camera.setParameters(parameters);

                int height = parameters.getPreviewSize().height;
                switch (height) {
                    case 480:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        break;
                    case 720:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                        break;
                    case 1080:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                        break;
                    default:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        break;
                }
                profile.videoFrameRate = parameters.getPreviewFrameRate();
                profile.videoFrameWidth = parameters.getPreviewSize().width;
                profile.videoFrameHeight = parameters.getPreviewSize().height;

                int size = CameraHelper.getStreamingImageWidth(context) * CameraHelper.getStreamingImageHeight(context);

                size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
                buffer = new byte[size];
                camera.addCallbackBuffer(buffer);

                try {
                    camera.setPreviewTexture(new SurfaceTexture(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                    //finishCamera();
                }
                IrHelper.setIrState(IrHelper.STATE_ON);



            } catch (Exception e) {
                Log.d("TAG", "Open camera failed: " + e);
            }
        }
    }

    public void initCamera() {
        if (!Utils.isCameraExist(context)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }
        deviceId = PreferencesHelper.getDeviceId(context);
        if (camera == null) {
            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception e) {
                Log.d("TAG", "Open camera failed: " + e);
            }
        }
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();

            //List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();

            int width = CameraHelper.getStreamingImageWidth(context);
            int height = CameraHelper.getStreamingImageHeight(context);

            parameters.setPreviewSize(width, height);
            int fps = CameraHelper.getStreamingFramerate(context);
            parameters.setPreviewFrameRate(fps);

            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);

            int size = width * height;

            size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            buffer = new byte[size];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera mCamera) {
                    Camera.Parameters parameters = camera.getParameters();

                    if (isStreamingRecording) {
                    streamingRecord(bytes);
                }

                    if (camera != null) {
                        camera.addCallbackBuffer(buffer);
                    }
                }
            });

            try {
                camera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Get camera from service failed");
        }
    }

    public void initPrimaryCamera() {
        if (!Utils.isCameraExist(context)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }
        deviceId = ConfigHelper.getDeviceName(context);
        if (primaryCamera == null) {
            //primaryCamera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);

            try {
                primaryCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception e) {
                Log.d("TAG", "Open camera failed: " + e);
            }
        }
        if (primaryCamera != null) {
            Camera.Parameters parameters = primaryCamera.getParameters();
            int fps =  CameraHelper.getLocalFramerate(context);
            int height =CameraHelper.getLocalImageHeight(context);
            int width = CameraHelper.getLocalImageWidth(context);

            switch (height) {
                case 480:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    profile.videoFrameWidth = width;
                    profile.videoFrameHeight = height;
                    break;
                case 720:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                    break;
                case 1080:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                    parameters.set("cam_mode", 1 );
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    break;
                default:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
            }

            parameters.setPreviewSize(width, height);
            parameters.setPreviewFpsRange(fps*1000, fps*1000);
            parameters.setPreviewFrameRate(fps);
            /*if(parameters.isAutoExposureLockSupported() && parameters.isAutoWhiteBalanceLockSupported()){
                parameters.setAutoExposureLock(true);
                parameters.setAutoWhiteBalanceLock(true);
            }*/

            //List<Camera.Size> previewSizes = primaryCamera.getParameters().getSupportedPreviewSizes();

            parameters.setPreviewFormat(ImageFormat.NV21);
            primaryCamera.setParameters(parameters);

            profile.videoBitRate = CameraHelper.getLocalVideoBitrate(context)*2000;
            profile.videoFrameRate = fps;
            //profile.videoFrameWidth = width;
            //profile.videoFrameHeight = height;
            profile.videoCodec = MediaRecorder.VideoEncoder.H264;
            //profile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;

            int size = width * height;

            size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            primaryBuffer = new byte[size];
            primaryCamera.addCallbackBuffer(primaryBuffer);
            primaryCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera mCamera) {


                    if (primaryCamera != null) {
                        primaryCamera.addCallbackBuffer(primaryBuffer);
                    }

                    if (takePhoto) {
                        takePhoto = false;
                        savePhoto(bytes);
                        machineHandler.sendEmptyMessage(StateMachineHandler.TAKE_PHOTO);
                    }
                }
            });
            try {
                primaryCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                primaryCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                //finishCamera();
            }
            IrHelper.setIrState(IrHelper.STATE_ON);
        } else {
            Log.d(TAG, "Get camera from service failed");
        }
    }

    public void initPhotoCamera(){
        //inicia la camara principal para tomar la foto
        if (!isStreamingRecording) {
            prepareStreamingCamera();//Abrir cámara 0 antes de la 1
        }
        initPrimaryCamera();


    }

    public void finishCamera() {
        if (!isStreamingRecording && !streamingRunAudioThread && !isSos) {
            try {
                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.lock();
                    camera.release();
                    camera = null;
                }
                //IrHelper.setIrState(IrHelper.STATE_OFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void setForceDestroy(){
        isForceDestroy = true;
        ServiceHelper.stopAllServices(context);
    }

    public void finishCameraDestroy() {
        try {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.lock();
                camera.release();
                camera = null;
                ServiceHelper.startAllServices(context);
            }
            //IrHelper.setIrState(IrHelper.STATE_OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void finishPrimaryCamera() {
        try {
            if (primaryCamera != null) {
                primaryCamera.stopPreview();
                primaryCamera.setPreviewCallback(null);
                primaryCamera.lock();
                primaryCamera.release();
                primaryCamera = null;
            }
            IrHelper.setIrState(IrHelper.STATE_OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configLocalMediaRecorder() {
        int fps =  CameraHelper.getLocalFramerate(context);
        primaryCamera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(primaryCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //mediaRecorder.setVideoEncodingBitRate(CameraHelper.getLocalVideoBitrate(context));
        int br = CameraHelper.getLocalVideoBitrate(context);
        mediaRecorder.setVideoEncodingBitRate(CameraHelper.getLocalVideoBitrate(context)*2000);

        //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        //# Video settings
        //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mediaRecorder.setProfile(profile);
        mediaRecorder.setVideoFrameRate(fps);
        //mediaRecorder.setCaptureRate(fps * 100);

        mediaRecorder.setOutputFile(VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath());
    }

    public void configStreamingRecorder() {
        streamingYuvImage = new Frame(CameraHelper.getStreamingImageWidth(context),
                CameraHelper.getStreamingImageHeight(context), Frame.DEPTH_UBYTE, 2);
        streamingRecorder = CameraHelper.initStreamingRecorder(context);
        String filterString = "transpose=dir=1:passthrough=portrait," +"drawtext=fontsize=15:fontfile=/system/fonts/DroidSans.ttf:fontcolor=white@0.8:text='TITAN-" +
                                PreferencesHelper.getDeviceId(context) + " %{localtime\\:%T %d/%m/%Y}':x=2:y=20,scale=w=" +
                                CameraHelper.getStreamingImageWidth(context) + ":h=" +
                                CameraHelper.getStreamingImageHeight(context);
        //String filterString = "transpose=dir=1:passthrough=portrait";
        streamingFilter = new FFmpegFrameFilter(filterString, CameraHelper.getStreamingImageWidth(context), CameraHelper.getStreamingImageHeight(context));
        streamingFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
        streamingAudioRecordRunnable = new StreamingAudioRecordRunnable();
        streamingAudioThread = new Thread(streamingAudioRecordRunnable);
        streamingRunAudioThread = true;
    }

    public void startLocalRecorder(boolean playSound) {
        startLocalMediaRecorder(playSound);
    }

    public void stopLocalRecorder(boolean playSound) {
        if (mediaRecorder != null) {
            stopLocalMediaRecorder(playSound);
        }
    }

    private void startLocalMediaRecorder(boolean playSound) {
        if (isLocalRecording) {
            return;
        }
        if (!playSound) {
            sequence = sequence + 1;
        }
        if (!isStreamingRecording) {
            prepareStreamingCamera();//Abrir cámara 0 antes de la 1
        }
        initPrimaryCamera();
        //Utils.updateWaterMark(context);
        configLocalMediaRecorder();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                    //Log.d("--onInfo", "Info " + i);
                }
            });
            isLocalRecording = true;
        } catch (IOException | IllegalStateException e) {
            //Log.e("Depuracion", "Error " + e.getMessage());
        }
        if (playSound) {
            if (PreferencesHelper.getLocalVibrateAndSound(context)) {
                CameraHelper.soundStart(context);
            }
            bus.post(new MessageEvent(MessageEvent.START_LOCAL_RECORDING));
        }
    }

    private void stopLocalMediaRecorder(boolean playSound) {
        if (mediaRecorder != null && isLocalRecording) {
            isLocalRecording = false;
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (RuntimeException e) {
                File file = new File(VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath());
                if (file.exists()) {
                    file.delete();
                }
                mediaRecorder.reset();
            } finally {
                mediaRecorder = null;
                if (!isStreamingRecording) {
                    finishCamera();
                }

                finishPrimaryCamera();

            }
        }
        if (playSound) {
            sendBusStopRecorder();
        }
    }

    public void sendBusStopRecorder() {
        videoDuration = 0;
        sequence = 1;
        if (ConfigHelper.getLocalVibrateAndSound(context)) {
            CameraHelper.soundStop(context);
        }
        bus.post(new MessageEvent(MessageEvent.STOP_LOCAL_RECORDING));
    }

    public void startStreamingRecorder() {
        if (isStreamingRecording) {
            return;
        }
        initCamera();
        configStreamingRecorder();
        try {
            streamingRecorder.start();
            streamingFilter.start();
            streamingStartTime = System.currentTimeMillis();
            isStreamingRecording = true;
            proccesingStreming = false;
            streamingAudioThread.start();
        } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
            e.printStackTrace();
            MessageEvent event = new MessageEvent(MessageEvent.STOP_STREAMING);
            bus.post(event);
        }
        if (PreferencesHelper.getStreamingVibrateAndSound(context)) {
            CameraHelper.soundStart(context);
        }
    }

    public void stopStreamingRecorder() {
        streamingRunAudioThread = false;
        if (!isLocalRecording) { // Si aún hay grabación local no cerrar la camara 0
            finishCamera();
        }
        try {
            if (streamingAudioThread != null) {
                streamingAudioThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        streamingAudioRecordRunnable = null;
        streamingAudioThread = null;
        if (streamingRecorder != null && isStreamingRecording) {
            isStreamingRecording = false;
            proccesingStreming = false;
            try {
                streamingRecorder.stop();
                streamingRecorder.release();
                streamingFilter.stop();
                streamingFilter.release();
            } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
                e.printStackTrace();
            }
            streamingRecorder = null;
            streamingFilter = null;
            streamingYuvImage = null;
        }
        if (ConfigHelper.getStreamingVibrateAndSound(context)) {
            CameraHelper.soundStop(context);
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera mCamera) {
        Camera.Parameters parameters = camera.getParameters();

        /*Mat mat = new Mat(parameters.getPreviewSize().height, parameters.getPreviewSize().width, CvType.CV_8UC2);
        mat.put(0, 0, bytes);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = sdf.format(new Date());
        WatermarkHelper.putWaterMark(mat, currentDate, "TITAN-" + deviceId);

        int bufferSize = (int) (mat.total() * mat.elemSize());
        byte[] b = new byte[bufferSize];

        mat.get(0, 0, b);*/

        if (isStreamingRecording) {
            streamingRecord(bytes);
        }

        if (camera != null) {
            camera.addCallbackBuffer(buffer);
        }
    }

    public void takePhoto() {
        takePhoto = true;
    }

    public void takePhotoDirect() {
        try{
            primaryCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.i(TAG, "onPictureTaken - raw");
                    //savePhoto(bytes);
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.i(TAG, "onPictureTaken - jpeg");
                    //se decodifica el arreglo de bytes para poner marca de agua y se vuelve a comprimir para guardarlo
                    Bitmap bmp = BitmapUtils.convertCompressedByteArrayToBitmap(bytes);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDate = sdf.format(new Date());

                    Bitmap bmpWm = WatermarkHelper.applyWaterMarkEffect(bmp, currentDate,"TITAN-" + deviceId, context);

                    byte[] data = BitmapUtils.convertBitmapToByteArray(bmpWm);
                    savePhotoDirect(data);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void savePhoto(byte[] data) {
        FileOutputStream file = null;
        try {
            Camera.Parameters parameters = primaryCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                    size.width, size.height, null);
            file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            image.compressToJpeg(
                    new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                    file);

            CameraHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePhotoDirect(byte[] data) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            file.write(data);
            CameraHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] bitmapToByteArray(Bitmap bm) {
        // Create the buffer with the correct size
        int iBytes = bm.getWidth() * bm.getHeight()*4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);

        // Log.e("DBG", buffer.remaining()+""); -- Returns a correct number based on dimensions
        // Copy to buffer and then into byte array
        bm.copyPixelsToBuffer(buffer);
        // Log.e("DBG", buffer.remaining()+""); -- Returns 0
        return buffer.array();
    }

    public void streamingRecord(byte[] data) {
        if (streamingAudioRecord == null || streamingAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            streamingStartTime = System.currentTimeMillis();
            return;
        }

        if (streamingRecorder != null && streamingYuvImage != null && isStreamingRecording) {
            ((ByteBuffer) streamingYuvImage.image[0].position(0)).put(data);
            try {
                long t = 1000 * (System.currentTimeMillis() - streamingStartTime);
                if (t > streamingRecorder.getTimestamp()) {
                    streamingRecorder.setTimestamp(t);
                }
                synchronized (this) {
                    //streamingRecorder.record(streamingYuvImage);
                    streamingFilter.push(streamingYuvImage);
                    Frame frame2;
                    while ((frame2 = streamingFilter.pull()) != null) {
                        streamingRecorder.record(frame2, streamingFilter.getPixelFormat());
                    }
                }
            } catch (FFmpegFrameRecorder.Exception | FrameFilter.Exception e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
                MessageEvent event = new MessageEvent(MessageEvent.STOP_STREAMING);
                bus.post(event);
            }
        }
    }

    public void initLocalVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterLocalVideo = new CounterLocalVideo(ConfigHelper.getLocalVideoDurationInMill(context), 1000);
            }
        });
    }

    public void sendSos() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraHelper.sendSignalSOS(context);
            }
        });
    }

    public void pauseLocalVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterLocalVideo != null) {
                    counterLocalVideo.cancel();
                }
            }
        });
    }

    public void startLocalVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterLocalVideo != null) {
                    counterLocalVideo.start();
                }
            }
        });
    }

    public void initPostVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterPostVideo = new CounterPostVideo(ConfigHelper.getLocalPostVideoDurationInMill(context), 1000);
            }
        });
    }

    public void pausePostVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterPostVideo != null) {
                    counterPostVideo.cancel();
                }
            }
        });
    }

    public void startPostVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterPostVideo != null) {
                    counterPostVideo.start();
                }
            }
        });
    }

    public void initFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterFlash = new CounterFlash(15000, 1000);
            }
        });
    }

    public void pauseFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterFlash != null) {
                    counterFlash.cancel();
                }
            }
        });
    }

    public void startFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterFlash != null) {
                    counterFlash.start();
                }
            }
        });
    }

    public void sendLogStreaming() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraHelper.sendLogStreaming(context, TrafficStats.getTotalTxBytes() - mStartTX, streamingStarted);
            }
        });
    }

    private class CounterLocalVideo extends CountDownTimer {
        CounterLocalVideo(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Message message = new Message();
            message.what = StateMachineHandler.LOCAL_RECORDER_PRESSED;
            message.arg1 = StateMachineHandler.DO_NOT_PLAY_SOUND;
            machineHandler.handleMessage(message);
            machineHandler.handleMessage(message);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            videoDuration += 1;
            int seconds = (int) millisUntilFinished / 1000;
            if (seconds % 10 == 0) {
                Utils.saveVideoLocation(context, VideoNameHelper.getCurrentNameFile(context));
            }
            showVideoDuration();
        }
    }

    private class CounterPostVideo extends CountDownTimer {
        CounterPostVideo(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Message message = new Message();
            message.what = StateMachineHandler.POST_RECORDING;
            machineHandler.handleMessage(message);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }

    private class CounterFlash extends CountDownTimer {
        CounterFlash(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            flashLightOff();
            flashOn = !flashOn;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }

    public void showVideoDuration() {
        if (videoDuration % 120 == 0) {
            CameraHelper.soundStart(context);
        }
        @SuppressLint("DefaultLocale")
        String value = String.format("%02d:%02d", videoDuration / 60, videoDuration % 60);
        MessageEvent event = new MessageEvent(MessageEvent.TIME_ELAPSED, value);
        bus.post(event);
    }

    public void flashLightOn() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                if (camera == null) {
                    final int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    camera = Utils.getCameraInstance(cameraId);
                }
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                initFlashCounter();
                startFlashCounter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                if (!isLocalRecording && !isStreamingRecording) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class StreamingAudioRecordRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            streamingAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            ShortBuffer shortBuffer = ShortBuffer.allocate(bufferSize);
            streamingAudioRecord.startRecording();
            while (streamingRunAudioThread) {
                int bufferResult = streamingAudioRecord.read(shortBuffer.array(), 0, shortBuffer.capacity());
                shortBuffer.limit(bufferResult);
                if (bufferResult > 0) {
                    if (isStreamingRecording) {
                        try {
                            streamingRecorder.recordSamples(shortBuffer);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            }
            Log.v(TAG, "AudioThread Finished, release audioRecord");
            if (streamingAudioRecord != null) {
                streamingAudioRecord.stop();
                streamingAudioRecord.release();
                streamingAudioRecord = null;
            }
        }
    }
}
