package com.example.meteosmsmanager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsListener extends BroadcastReceiver {
    String sms_text;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        Log.d("Debug",msgBody + " / " + msg_from);

                        String text = msgBody.replaceAll("[^0-9., ]"," ");
                        text = text.replace(",",".");
                        String _text = text.trim().replaceAll(" +", " ");
                        sms_text = msgBody;

                        if(_text.length()>3){
                            soil_temp(msg_from, _text);
                        }else{
                            precipitation(msg_from, _text);
                        }
                    }
                }catch(Exception e){
                    Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }

    public void rejected(String phone, String text) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String _phone = phone.substring(4,phone.length());
        String sign = MD5(_phone + currentDate + "bCctS9eqoYaZl21a");
        String _text = convertStringToUTF8(text);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://wwcs.tj/meteo/rejected.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("sign", sign);
                    jsonParam.put("datetime", currentDate);
                    jsonParam.put("phone", _phone);
                    jsonParam.put("smstext", _text);
                    jsonParam.put("state", "rejected");

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    Log.i("MSG" , sb.toString());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void soil_temp(String phone, String text) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String _phone = phone.substring(4,phone.length());
        String sign = MD5(_phone + currentDate + "bCctS9eqoYaZl21a");

        String type;
        Date check_date = new Date();
        int check_time = check_date.getHours();

        if(check_time>13){
            type = "E";
        }else{
            type = "M";
        }

        Thread thread = new Thread(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                try {
                    URL url = new URL("https://wwcs.tj/meteo/soiltemp.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    String[] massive = text.split(" ");
                    double soil1, soil2, soil3;
                    String a,b,c;
                    a = massive[0];
                    b = massive[1];
                    c = massive[2];

                    if(a.substring(a.length() - 1).equals(".")){
                        a = a.substring(0, a.length() - 1);
                    }
                    if(b.substring(b.length() - 1).equals(".")){
                        b = b.substring(0, b.length() - 1);
                    }
                    if(c.substring(c.length() - 1).equals(".")){
                        c = c.substring(0, c.length() - 1);
                    }

                    soil1 = Double.parseDouble(a);
                    soil2 = Double.parseDouble(b);
                    soil3 = Double.parseDouble(c);

                    JSONObject jsonObject = new JSONObject("{\"soil_1\":"+soil1+",\"soil_2\":"+soil2+",\"soil_3\":"+soil3+"}");

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("sign", sign);
                    jsonParam.put("datetime", currentDate);
                    jsonParam.put("phone", _phone);
                    jsonParam.put("smstext", text);
                    jsonParam.put("type", type);
                    jsonParam.put("data", jsonObject);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    Log.i("MSG" , sb.toString());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    rejected(phone, sms_text);
                }
            }
        });
        thread.start();
    }

    public void precipitation(String phone, String text) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String _phone = phone.substring(4,phone.length());
        String sign = MD5(_phone + currentDate + "bCctS9eqoYaZl21a");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://wwcs.tj/meteo/precipitation.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("sign", sign);
                    jsonParam.put("datetime", currentDate);
                    jsonParam.put("phone", _phone);
                    jsonParam.put("smstext", text);
                    jsonParam.put("precipitation", text);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    Log.i("MSG" , sb.toString());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    rejected(phone, sms_text);
                }
            }
        });
        thread.start();
    }

    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    public static String convertStringToUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }
}