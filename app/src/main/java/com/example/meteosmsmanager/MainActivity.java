package com.example.meteosmsmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.meteosmsmanager.databinding.ActivityMainBinding;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class MainActivity extends AppCompatActivity  {
    private ActivityMainBinding binding;
    ArrayList<MyData> farmers;
    boolean getMorning = false;
    //boolean getPrecipitation = false;
    boolean getEvening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        farmers = new ArrayList<>();

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            SSLEngine engine = sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        final String[] current_time = new String[1];

        new CountDownTimer(300000, 100000)
        {
            @Override
            public void onTick(long millisUntilFinished) {
                current_time[0] = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                //Log.i("Debug1", current_time[0]);
                //Log.i("Debug2", String.valueOf(farmers.size()));

                switch (current_time[0]) {
                    case "07":
                        //Log.i("Debug", "Evening Time " + current_time[0]);
                        getMorning = true;
                        if (farmers.size() == 0) {
                            getList();
                            binding.infoField.setText(current_time[0] + "\nList of farmers updated\nMorning Soil Temp.");
                        }
                        break;
                    case "08":
                        if (getMorning) {
                            if (farmers.size() > 0) {
                                //Log.i("Debug", "SMS morning sent! " + current_time[0]);
                                SendSMS_M();
                                getMorning = false;
                                binding.infoField.setText(current_time[0] + "\nSMS Sent for Morning\nSoil Temp.");
                            }
                        }
                        break;
                    /*case "08:20":
                        //Log.i("Debug", "Precipitation Time " + current_time[0]);
                        getPrecipitation = true;
                    if (farmers.size() == 0) {
                        getList();
                        binding.infoField.setText(current_time[0] + "\nList of farmers updated\nPrecipitation");
                    }
                        break;
                    case "08:21":
                        if (getPrecipitation) {
                            if (farmers.size() > 0) {
                                //Log.i("Debug", "SMS precipitation sent! " + current_time[0]);
                                SendSMS_P();
                                getPrecipitation = false;
                                binding.infoField.setText(current_time[0] + "\nSMS Sent for Precipitation");
                            }
                        }
                        break;*/
                    case "15":
                        //Log.i("Debug", "Evening Time " + current_time[0]);
                        getEvening = true;
                        if (farmers.size() == 0) {
                            getList();
                            binding.infoField.setText(current_time[0] + "\nList of farmers updated\nEvening Soil Temp.");
                        }
                        break;
                    case "16":
                        if (getEvening) {
                            if (farmers.size() > 0) {
                                //Log.i("Debug", "SMS evening sent! " + current_time[0]);
                                SendSMS_E();
                                getEvening = false;
                                binding.infoField.setText(current_time[0] + "\nSMS Sent for Evening\nSoil Temp.");
                            }
                        }
                        break;
                }
                start();
            }
        }.start();
    }

    public void SendSMS_M(){
            //Log.i("Massive", String.valueOf(farmers.size()));
            new CountDownTimer(8000, 2000)
            {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (farmers.size() > 0){
                        ActionSend(farmers.get(0).phone, "Илтимос маълумоти харорати хокро барои субх пешниход кунед (8:00-9:00)");
                        //Log.i("Massive", String.valueOf(farmers.size()));
                        //Log.i("Massive", farmers.get(0).phone);
                        farmers.remove(0);
                        start();
                    }
                }
            }.start();
    }

    public void SendSMS_P(){
        //Log.i("Massive", String.valueOf(farmers.size()));
        new CountDownTimer(8000, 2000)
        {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (farmers.size() > 0){
                    ActionSend(farmers.get(0).phone, "Илтимос маълумоти боришотро пешниход кунед (8:00-9:00)");
                    //Log.i("Massive", String.valueOf(farmers.size()));
                    //Log.i("Massive", farmers.get(0).phone);
                    farmers.remove(0);
                    start();
                }
            }
        }.start();
    }

    public void SendSMS_E(){
            new CountDownTimer(8000, 2000)
            {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (farmers.size() > 0){
                        ActionSend(farmers.get(0).phone, "Маълумоти харорати хокро барои бегох пешниход кунед (16:00-15:00)");
                        //Log.i("Massive", String.valueOf(farmers.size()));
                        //Log.i("Massive", farmers.get(0).phone);
                        farmers.remove(0);
                        start();
                    }
                }
            }.start();
    }

    private void ActionSend(String phoneNumber, String message){
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        /*Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();*/
                        //Log.i("Debug", "SMS sent! "+phoneNumber);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver(){
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        /*Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();*/
                        //binding.infoField.setText("SMS delivered to\n"+phoneNumber);
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void getList() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String sign = MD5(currentDate + "bCctS9eqoYaZl21a");
        farmers.clear();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://wwcs.tj/meteo/farmers.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("sign", sign);
                    jsonParam.put("datetime", currentDate);

                    //Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    //Log.i("STATUS", String.valueOf(conn.getResponseCode()));

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    conn.disconnect();
                    String response = sb.toString();

                    if (response != null) {
                        //Log.i("MSG" , response);
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject fromJson = array.getJSONObject(i);
                                MyData data = new MyData();
                                data.phone = fromJson.getString("phone");
                                farmers.add(data);
                            }

                            //Log.i("massive" , String.valueOf(farmers.size()));
                        } catch(Exception e){
                            Log.e("Debug", "Error in parsing");
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Server is not responding!", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
}