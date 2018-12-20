package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.model.CycleCountWeekDay;
import com.bpt.tipi.streaming.model.CycleCount;
import com.bpt.tipi.streaming.persistence.ChargeCycleBattery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChargeCycleCounterBroadcastReceicver extends BroadcastReceiver {

    private float a(Context context) {
        float f = 0.0f;
        try {
            //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.openFileInput("batteryStat.txt")));
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            File logStorageDir = new File(Environment.getExternalStoragePublicDirectory("DCIM"), "LOG");
            if (!logStorageDir.exists()) {
                if (!logStorageDir.mkdirs()) {
                    Log.i("Depuracion", "Error al crear el directorio");
                }
            }

            File file = new File(logStorageDir, "BatteryStat.txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine != null) {
                        Toast.makeText(context,"-- " + readLine, Toast.LENGTH_LONG).show();
                        stringBuilder.append(readLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    break;
                } catch (NumberFormatException e2) {
                    e2.printStackTrace();
                }
            }
            f = Float.parseFloat(stringBuilder.toString());
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return f;
    }

    private String a() {
        return new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
    }

    private void a(float f, Context context) {
        //File file = new File(context.getFilesDir(), "batteryStat.txt");
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        File logStorageDir = new File(Environment.getExternalStoragePublicDirectory("DCIM"), "LOG");
        if (!logStorageDir.exists()) {
            if (!logStorageDir.mkdirs()) {
                Log.i("Depuracion", "Error al crear el directorio");
            }
        }

        File file = new File(logStorageDir, "BatteryStat.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        Float f2 = new Float(f);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(f2.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage() + " Unable to write to internal storage.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int i = 100;
        Intent registerReceiver = context.getApplicationContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        String action = intent.getAction();
        int intExtra = registerReceiver.getIntExtra("level", 0);
        Integer valueOf = Integer.valueOf((intExtra * 100) / registerReceiver.getIntExtra("scale", 100));
        ChargeCycleBattery rVar = new ChargeCycleBattery(context);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean valueOf2 = Boolean.valueOf(defaultSharedPreferences.getBoolean("checkboxToast", false));
        if (!Boolean.valueOf(defaultSharedPreferences.getBoolean("checkboxDisableStats", false)).booleanValue()) {
            if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                if (!valueOf2.booleanValue()) {
                    Toast.makeText(context, context.getResources().getString(R.string.charger_conected_message) + " " + valueOf + "%", Toast.LENGTH_LONG).show();
                }
                a();
                try {
                    //BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(context.getFilesDir(), "batteryTemp.txt")));
                    File logStorageDir = new File(Environment.getExternalStoragePublicDirectory("DCIM"), "LOG");
                    if (!logStorageDir.exists()) {
                        if (!logStorageDir.mkdirs()) {
                            Log.i("Depuracion", "Error al crear el directorio");
                        }
                    }

                    File file = new File(logStorageDir, "BatteryStat.txt");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                    bufferedWriter.write(valueOf.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    Toast.makeText(context, e.getMessage() + " Unable to write to storage.", Toast.LENGTH_LONG).show();
                }
            } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                try {
                    InputStream openFileInput = context.openFileInput("batteryTemp.txt");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openFileInput));
                    StringBuilder stringBuilder = new StringBuilder();
                    while (true) {
                        try {
                            String readLine = bufferedReader.readLine();
                            if (readLine == null) {
                                break;
                            }
                            stringBuilder.append(readLine);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (!valueOf2.booleanValue()) {
                        Toast.makeText(context, context.getResources().getString(R.string.charger_disconnected_message) + " " + intExtra + "%", Toast.LENGTH_LONG).show();
                    }
                    try {
                        i = Integer.parseInt(stringBuilder.toString());
                        openFileInput.close();
                    } catch (NumberFormatException e3) {
                        e3.printStackTrace();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    if (intExtra > i) {
                        float f = (((float) intExtra) - ((float) i)) / 100.0f;
                        a(a(context) + f, context);
                        action = a();
                        rVar.a(new CycleCount(action, rVar.a(action).getCycleCount() + f));
                        try {
                            int day = new SimpleDateFormat("yyyy/MM/dd").parse(action).getDay();
                            CycleCountWeekDay a = rVar.a(day);
                            String a2 = a.getLastDate();
                            float b = a.getWeekdayCycleCount();
                            int c = a.getNumOfWeekdays();
                            if (a2 == null) {
                                a.setWeekday(day);
                                c++;
                                a2 = action;
                            }
                            if (a2.equals(action)) {
                                f += b;
                                action = a2;
                            } else {
                                c++;
                                f += b;
                            }
                            a.setNumOfWeekdays(c);
                            a.setLastDate(action);
                            a.setWeekdayCycleCount(f);
                            rVar.a(a);
                        } catch (ParseException e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e5.printStackTrace();
                }
                //context.deleteFile("batteryTemp.txt");
            }
            rVar.close();
        }
    }
}
