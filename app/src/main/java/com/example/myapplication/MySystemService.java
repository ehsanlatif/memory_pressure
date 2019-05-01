package com.example.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import android.os.SystemClock;
import android.support.annotation.FontRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.ContentValues.TAG;

public class MySystemService extends Service {
    static {
        System.loadLibrary("native-lib");
    }

    public String memoryStat(int level) {

        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                return "";
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE: {
                Log.d(TAG, "on Moderat Pressure");
                return "Moderat";
            }
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW: {
                Log.d(TAG, "on High Pressure");
                return "High";
            }

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:{
                Log.d(TAG,"on Critical Pressure");
                return "Critical";
            }

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            {
                //Log.d(TAG, "on Moderat Pressure");
                return"Same";
            }

            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            {
                Log.d(TAG, "on Moderat Pressure");
                return"Moderat";
            }
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                Log.d(TAG, "LMKD kicked In");
                return"LMKD kicked In";

            default:
                return "same";
        }
    }
    public native int PassSizeToNative(int a,boolean b);

    private static MySystemService instance = null;
    String fileName;
    int duration;
    boolean repeat;
    String processName;
    int pressure;
    int init_pressure;
    private static int initial_Cache;



    public static boolean isInstanceCreated(){
        return instance != null;
    }
    //
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Memory Pressure is applied to your system",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Intent playIntent = new Intent(this, MySystemService.class);
            playIntent.setAction("Stop Service");
            PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
            NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.button_onoff_indicator_on, "Stop", pendingPlayIntent);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("MP Simulator")
                    .setSmallIcon(R.drawable.ic_memory_black_24dp)
                    //.setContentIntent(pendingPlayIntent)
                    .addAction(playAction)
                    .setContentText("Memory Pressure applied").build();

            startForeground(1, notification);
        }
    }



    int []pids={0,0};
    boolean first_run=false;
    constPressureTaskExecutor constPressureTaskExecutor;
    public static boolean isStatred=false;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if(intent.getAction()!=null && intent.getAction().equals("Stop Service"))
        {
            stopForeground(true);
            stopSelf();
            stopService(new Intent(getApplicationContext(),MySystemService.class));
        }else {
            Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_SHORT).show();
            final ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
            constPressureTaskExecutor = new constPressureTaskExecutor(activityManager);
            pids[0] = android.os.Process.myPid();
            String[] cmd2 = {"su", "-c", "toybox renice -n -30 -p " + pids[0]};
            Process p1 = null;
            try {
                p1 = Runtime.getRuntime().exec(cmd2);
                p1.waitFor();
                p1.destroy();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    constPressureTaskExecutor.execute();
                }
            }).start();


            if (intent != null && intent.getExtras() != null) {
                fileName = intent.getStringExtra("filename");
                duration = intent.getIntExtra("duration", 0);
                repeat = intent.getBooleanExtra("repeat", false);
                processName = intent.getStringExtra("process");
                pressure = intent.getIntExtra("pressure", 0);
                init_pressure = intent.getIntExtra("initial_pressure", 0);

                try {
//                if (activityManager.isLowRamDevice())
//                    Toast.makeText(getApplicationContext(), "Is LowDevice", Toast.LENGTH_LONG).show();
                    List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
                    for (AndroidAppProcess process : processes) {
                        if (process.name.equals(processName)) {
                            pids[1] = process.stat().getPid();
                            break;
                        }
                    }

                    if (pids[1] == 0) {
                        String[] cmd = {"su", "-c", "pidof " + processName};
                        Process proc2 = Runtime.getRuntime().exec(cmd);
                        InputStream upis = proc2.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(upis));
                        pids[1] = Integer.parseInt(br.readLine().toString());
                    }


                    first_run = true;
                    if (processName == null || pids[1] != 0) {
                        instance = this;
                        SaveText(fileName + ".csv", "time," + "pressure_pss(MB)" + "," + processName + "_pss(MB)," + "Active_Memory(MB)" + "," + "Cached_Memory(MB)" + "," + "Free_Memory(MB)" + "," + "VM_Pressure" + "\n");
                        if (repeat == true) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // myAsyncTask.execute();
                                    while (true) {

                                        Log.i(TAG, "Calling JNI");

                                        List<Integer> list = findMemoryStats(activityManager);

                                        if (first_run)
                                            initial_Cache = list.get(3) + list.get(4);

                                        if ((list.get(3) + list.get(4)) > initial_Cache)
                                            initial_Cache = list.get(3) + list.get(4);
                                        double vmPressure = (double) (((initial_Cache - (list.get(3) + list.get(4))) * 100) / initial_Cache);
                                        Log.w(TAG, "VM_Pressure: " + vmPressure + "%");


                                        Date date = new Date();
                                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                        Log.i(TAG, String.format("**** Time: %s ==> Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n", dateFormat.format(date), list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
                                        SaveText(fileName + ".csv", dateFormat.format(date) + "," + list.get(0) + "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "," + vmPressure + "\n");

                                    if (first_run == true)
                                        PassSizeToNative(init_pressure * 1024 * 1024, repeat);
                                    else
                                        PassSizeToNative(pressure * 1024 * 1024, repeat);

                                        first_run = false;
                                        try {
                                            Thread.sleep(duration);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                        } else {
                            List<Integer> list = findMemoryStats(activityManager);

                            initial_Cache = list.get(3) + list.get(4);

                            double vmPressure = (double) (((initial_Cache - (list.get(3) + list.get(4))) * 100) / initial_Cache);
                            Log.w(TAG, "VM_Pressure: " + vmPressure + "%");


                            Date date = new Date();
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                            Log.i(TAG, String.format("**** Time: %s ==> Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n", dateFormat.format(date), list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
                            SaveText(fileName + ".csv", dateFormat.format(date) + "," + list.get(0) + "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "," + vmPressure + "\n");

                            Log.i(TAG, "Calling JNI");
                            if (init_pressure == -1)
                                pressure = pressure * initial_Cache / 100;
                            PassSizeToNative(pressure * 1024 * 1024, repeat);

//                    List<Integer> list2=findMemoryStats(activityManager);
//
//                    double newVmPressure = (double) (((initial_Cache - (list2.get(3) + list2.get(4))) * 100) / initial_Cache);
//                    Log.w(TAG, "VM_Pressure: " + newVmPressure + "%");
//
//
//                    date = new Date();
//                    Log.i(TAG, String.format("**** Time: %s ==> Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n", dateFormat.format(date), list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
//                    SaveText(fileName + ".csv", dateFormat.format(date) + "," + list.get(0) + "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "," + newVmPressure + "\n");


                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Process name : " + processName + " is not running", Toast.LENGTH_SHORT).show();
                        onDestroy();

                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            isStatred = true;
        }
        return START_STICKY_COMPATIBILITY;

    }

    public class constPressureTaskExecutor extends AsyncTask<Integer,Void,Void>
    {
        ActivityManager activityManager;
        constPressureTaskExecutor(ActivityManager activityManager)
        {
            this.activityManager=activityManager;
        }
        @Override
        protected Void doInBackground(Integer... integers) {
            while (true) {
                try {
                    List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
                    for (AndroidAppProcess process : processes) {
                        if (pids[0] == process.stat().getPid() && process.oom_adj() >= 0) {
                            Log.e(TAG, "Changing OOM");
                            String[] cmd = {"su", "-c", "echo -17 > /proc/" + pids[0] + "/oom_adj"};
                            Process p = Runtime.getRuntime().exec(cmd);
                            p.waitFor();
                            p.destroy();
                            break;
                        }
                    }
                    Thread.sleep(2000);
                } catch(InterruptedException e1){
                    e1.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public List<Integer> findMemoryStats(ActivityManager activityManager)
    {
        android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
        try {
            Process proc = Runtime.getRuntime().exec("cat /proc/meminfo " + pids[1]);

            InputStream is = proc.getInputStream();
            Map<String, Integer> memMap = getStringFromInputStream(is, 2);
            ActivityManager.RunningAppProcessInfo rapI = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(rapI);
            String state = memoryStat(rapI.lastTrimLevel);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            if (memoryInfo.lowMemory) {
                Log.e(TAG, "low memory and threshold:" + memoryInfo.threshold);

            }

            List<Integer> list = new ArrayList<>();
            list.add(memoryInfoArray[0].getTotalPss() / 1024);
            list.add(memoryInfoArray[1].getTotalPss() / 1024);
            list.add(memMap.get("Active:") / 1024);
            list.add(memMap.get("Cached:") / 1024);
            list.add(memMap.get("MemFree:") / 1024);
            list.add(memMap.get("MemTotal:") / 1024);
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void SaveText(String sFileName, String sBody){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);

            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(sBody);
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Log.d(TAG,e.getMessage());
        }}
    private static Map<String,Integer> getStringFromInputStream(InputStream is,int oneLine) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        Map<String,Integer> Map=new HashMap<>();

        try {
            while((line = br.readLine()) != null) {
                String[] strs = line.split(" ");
                if(oneLine == 0)
                {
                    Map.put("pid", Integer.parseInt(strs[1]));
                }
                else if(oneLine==1)
                {
                    Map.put("utime", Integer.parseInt(strs[13]));
                    Map.put("stime", Integer.parseInt(strs[14]));
                    Map.put("cutime", Integer.parseInt(strs[15]));
                    Map.put("cstime", Integer.parseInt(strs[16]));
                    Map.put("starttime", Integer.parseInt(strs[21]));
                }else if(oneLine ==2 ){
                    Map.put(strs[0], Integer.parseInt(strs[strs.length - 2]));
                }
                sb.append(line);
                sb.append("\n");
            }
        }
        catch (IOException e) {
            Log.e(TAG, "------ getStringFromInputStream " + e.getMessage());
        }
        finally {
            if(br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "------ getStringFromInputStream " + e.getMessage());
                }
            }
        }

        return Map;
    }


    @Override
    public void onDestroy() {

        if(constPressureTaskExecutor!=null)
            constPressureTaskExecutor.cancel(true);
        PassSizeToNative(0,false);

//        Intent serviceIntent=new Intent(getApplicationContext(),MySystemService.class);
//        serviceIntent.putExtra("filename",fileName);
//        serviceIntent.putExtra("duration", duration);
//        serviceIntent.putExtra("repeat", repeat);
//        serviceIntent.putExtra("process", processName);
//        serviceIntent.putExtra("pressure",pressure);
//        serviceIntent.putExtra("initial_pressure", init_pressure);

        instance = null;
    }
}
