package com.bpt.tipi.streaming.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P;

/**
 * Created by jpujolji on 26/11/17.
 */

public class WatermarkHelper {

    public static final int RECORDER_TYPE_LOCAL = 0;
    public static final int RECORDER_TYPE_STREAMING = 1;

    public static final String FORMAT_MP4 = "mp4";
    public static final String FORMAT_FLV = "flv";

    private static final Scalar TEXT_COLOR = new Scalar(255, 255, 255);

    public static void putWaterMark(Mat mat, String text, String text2) {

        //Calculate size of new matrix
        /*double radians = Math.toRadians(270);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) (mat.width() * cos + mat.height() * sin);
        int newHeight = (int) (mat.width() * sin + mat.height() * cos);

        // rotating image
        Point center = new Point(newWidth / 2, newHeight / 2);


        //Creating the transformation matrix M
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, 270, 1);*/
        switch (mat.rows()) {
            case 240:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 10), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 115), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                break;
            case 288:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 10), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 140), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                break;
            case 320:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 10), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 150), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                break;
            case 480:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 20), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.7, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 230), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.7, TEXT_COLOR);
                break;
            case 600:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 20), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 290), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                break;
            case 720:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 20), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 340), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                break;
            case 1080:
                //Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 30), Core.FONT_HERSHEY_COMPLEX_SMALL, 1.5, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 500), Core.FONT_HERSHEY_COMPLEX_SMALL, 1.5, TEXT_COLOR);
                break;
        }
    }

    public static Bitmap applyWaterMarkEffect(Bitmap src, String fecha , String watermark, Context context) {
        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap.Config conf = src.getConfig();
        Bitmap result = Bitmap.createBitmap(w, h, conf);

        Canvas canvas = new Canvas(result);

        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.white));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawBitmap(src, 0, 0, paint);
        paint.setTextSize(30);
        //canvas.drawText(fecha, 20, 50, paint);
        //canvas.save();
        //canvas.rotate(-90, 800, 710);
        canvas.drawText(watermark + " " + fecha, 100, 50, paint);
        //canvas.restore();



        return result;
    }
}