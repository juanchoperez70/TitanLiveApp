package com.bpt.tipi.streaming;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.model.UrlsConfig;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ConfigReaderHelper {
    private static final Pattern PATTERN_IP = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final Pattern PATTERN_URL = Pattern.compile(
            "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private static final Pattern PATTERN_STRING = Pattern.compile(
            "^[a-zA-Z0-9]*");

    private static final Pattern PATTERN_PORT = Pattern.compile(
    "^\\d+$");

    private static final Pattern PATTERN_PWD = Pattern.compile(
            "^[a-zA-Z0-9]*");

    public static void writeTxt(String text) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/CONFIG/url_config_failed.txt";


        File logFile = new File(path);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void readFile(Context context){
        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        File sdcard = Environment.getExternalStorageDirectory();
        String[] configs = context.getResources().getStringArray(R.array.url_configs);

        //Get the text file
        File file = new File(sdcard,"/urlsConfig.txt");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String config = line.split(Pattern.quote(":"))[0];
                boolean contains = Arrays.asList(configs).contains(config);
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            e.printStackTrace();
        }
    }

    public static void deleteFile(Context context) throws IOException {
        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        File sdcard = Environment.getExternalStorageDirectory();

        //Get the text file
        File file = new File(sdcard,"/DCIM/CONFIG/urlsConfig.yaml");

        if(file.exists()){
            file.delete();
            if(file.exists()) {
                file.getCanonicalFile().delete();
                context.getApplicationContext().deleteFile(file.getName());
            }
        }
    }

    public static boolean loadConfig(Context context) throws Exception {
        boolean isConfig = false;
        int fail = 11;
        /*String ruta = new File("").getAbsolutePath();
        String separador = (ruta.contains("\\")) ? "\\" : "/";
        String rutaLocal = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (rutaLocal.equals("")) {
            rutaLocal = ruta + separador;
        } else {
            rutaLocal += separador;
        }*/

        File logStorageDir = new File(Environment.getExternalStoragePublicDirectory("DCIM"), "CONFIG");
        if (!logStorageDir.exists()) {
            if (!logStorageDir.mkdirs()) {
                Log.i("Depuracion", "Error al crear el directorio");
            }
        }

        File configFile = new File(logStorageDir + "/urlsConfig.yaml");
        if(configFile.exists()) {
            InputStream targetStream = new FileInputStream(configFile);
            Yaml yaml = new Yaml(new Constructor(UrlsConfig.class));

            UrlsConfig urlsConfig = yaml.load(targetStream);

            if (validateUrl(urlsConfig.getUrlApi())) {
                PreferencesHelper.setUrlApi(context, urlsConfig.getUrlApi());
                fail--;
            }
            else {
                writeTxt("URL API invalida");
            }
            if (validateUrl(urlsConfig.getUrlTitan())) {
                PreferencesHelper.setUrlTitan(context, urlsConfig.getUrlTitan());
                fail--;
            }
            else {
                writeTxt("URL TITAN invalida");
            }
            if (validateIp(urlsConfig.getUrlMqtt())) {
                PreferencesHelper.setUrlMqtt(context, urlsConfig.getUrlMqtt());
                fail--;
            }
            else {
                writeTxt("URL MQTT invalida");
            }
            if (validatePort(urlsConfig.getPortMqtt())) {
                PreferencesHelper.setPortMqtt(context, urlsConfig.getPortMqtt());
                fail--;
            }
            else {
                writeTxt("PUERTO MQTT invalido");
            }
            if (validateString(urlsConfig.getUsernameMqtt())) {
                PreferencesHelper.setUsernameMqtt(context, urlsConfig.getUsernameMqtt());
                fail--;
            }
            else{
                writeTxt("USUARIO MQTT invalido");
            }
            if (validatePwd(urlsConfig.getPasswordMqtt())) {
                PreferencesHelper.setPasswordMqtt(context, urlsConfig.getPasswordMqtt());
                fail--;
            }
            else{
                writeTxt("CONTRASEÑA MQTT invalida");
            }
            if (validateIp(urlsConfig.getUrlStreaming())) {
                PreferencesHelper.setUrlStreaming(context, urlsConfig.getUrlStreaming());
                fail--;
            }
            else{
                writeTxt("URL WOWZA invalida");
            }
            if (validatePort(urlsConfig.getPortStreaming())) {
                PreferencesHelper.setPortStreaming(context, urlsConfig.getPortStreaming());
                fail--;
            }
            else{
                writeTxt("PUERTO WOWZA invalido");
            }
            if (validateString(urlsConfig.getAppNameStreaming())) {
                PreferencesHelper.setAppNameStreaming(context, urlsConfig.getAppNameStreaming());
                fail--;
            }
            else{
                writeTxt("WOWZA NAME invalido");
            }
            if (validateString(urlsConfig.getUsernameMqtt())) {
                PreferencesHelper.setUsernameStreaming(context, urlsConfig.getUsernameStreaming());
                fail--;
            }
            else{
                writeTxt("NOBRE USUARIO invalido");
            }
            if (validatePwd(urlsConfig.getPasswordStreaming())) {
                PreferencesHelper.setPasswordStreaming(context, urlsConfig.getPasswordStreaming());
                fail--;
            }
            else{
                writeTxt("CONTRASEÑA WOWZA invalida");
            }
            if (fail == 0) {
                isConfig = true;

            }
        }
        return isConfig;
    }

    private static boolean validateIp(final String ip) {
        return PATTERN_IP.matcher(ip).matches();
    }

    private static boolean validateUrl(final String url) {
        return PATTERN_URL.matcher(url).matches();
    }

    private static boolean validateString(final String text) {
        return PATTERN_STRING.matcher(text).matches();
    }

    private static boolean validatePort(final String port) {
        return PATTERN_PORT.matcher(port).matches();
    }

    private static boolean validatePwd(final String pwd) {
        return PATTERN_PWD.matcher(pwd).matches();
    }
}
