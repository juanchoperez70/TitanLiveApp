package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.bpt.tipi.streaming.ConfigReaderHelper;
import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.activity.MainActivity;
import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.model.Label;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;
import com.bpt.tipi.streaming.persistence.Database;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class USBConnectionReceiver extends BroadcastReceiver {

    public static boolean firstConnect = true;
    public static final String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";

    EventBus bus = EventBus.getDefault();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(12000, 12000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    String action = intent.getAction();

                    Toast.makeText(context, ".:USB:.", Toast.LENGTH_SHORT).show(); //Debug

                    /*
                    StringBuilder builder = new StringBuilder("Extras:\n");
                    for (String key : intent.getExtras().keySet()) { //extras is the Bundle containing info
                        Object value = intent.getExtras().get(key); //get the current object
                        builder.append(key).append(": ").append(value).append("\n"); //add the key-value pair to the
                    }
                    Toast.makeText(context, builder.toString(), Toast.LENGTH_LONG).show(); //Debug
                    */

                    if (action.equalsIgnoreCase(usbStateChangeAction)) { //Check if change in USB state
                        if (intent.getExtras().getBoolean("connected")) {
                            // USB was connected
                            ServiceHelper.stopAllServices(context);
                            IrHelper.setIrState(IrHelper.STATE_OFF);
                            Toast.makeText(context, ".:Se detienen los servicios:.", Toast.LENGTH_SHORT).show();
                        } else {
                            // USB was disconnected
                            leerConfiguracionUrls(context);
                            loadLabels(context);
                            ServiceHelper.startAllServices(context);
                            Toast.makeText(context, ".:Se inician los servicios:.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    firstConnect = true;
                }
            }.start();
        }
    }

    private void leerConfiguracionUrls(Context context){
        try {
            boolean isConfig = ConfigReaderHelper.loadConfig(context);
            if(isConfig){
                //borrar el archivo de configuuracion
                ConfigReaderHelper.deleteFile(context);
            }

            System.out.print("Configuraciones de url realizadas: " + isConfig);
        } catch (Exception e) {
            ConfigReaderHelper.writeTxt(e.getMessage());
            Toast.makeText(context, "Error al leer archivo de configuracion URL", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void loadLabels(final Context context) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpClient httpClient = new HttpClient(context,new HttpInterface() {
            @Override
            public void onSuccess(String method, JSONObject response) {
                switch (method) {
                    case HttpHelper.Method.LABELS:
                        JSONObject object = response.optJSONObject("result");
                        if (object != null && object.optString("code", "").equals("100")) {
                            JSONArray jsonArray = response.optJSONArray("labelsList");
                            if (jsonArray != null) {
                                Database database = new Database(context);
                                try {
                                    database.open();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Gson gson = new Gson();
                                Type collectionType = new TypeToken<List<Label>>() {
                                }.getType();
                                List<Label> labels = gson.fromJson(jsonArray.toString(), collectionType);
                                database.insertLabels(labels);
                                database.close();
                            }
                        }
                    break;
                }
            }

            @Override
            public void onFailed(String method, JSONObject errorResponse) {
                //Toast.makeText(context,"Error al consultar los tags " + errorResponse.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        httpClient.httpRequest(json.toString(), HttpHelper.Method.LABELS, HttpHelper.TypeRequest.TYPE_POST, true);

    }
}
