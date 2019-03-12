package com.example.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class MySystemService extends Service {
    static {
        System.loadLibrary("native-lib");
    }
    public native void PassSizeToNative(int a,boolean b);

    private static MySystemService instance = null;
    private final IBinder mBinder = new LocalBinder();
    String fileName;
    int duration;
    boolean repeat;
    String processName;
    int pressure;
    Callbacks activity;


    // Timer timer = new Timer();
    ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    OutputStreamWriter osw;


    public static boolean isInstanceCreated(){
        return instance != null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
                return Service.START_STICKY;
    }
    int []pids={0,0};
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getExtras()!=null) {
            fileName = intent.getStringExtra("filename");
            duration = intent.getIntExtra("duration", 0);
            repeat = intent.getBooleanExtra("repeat", false);
            processName = intent.getStringExtra("process");
            pressure = intent.getIntExtra("pressure", 0);
        }
        pids[0] = android.os.Process.myPid();
        try {

            final ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> activityes = ((ActivityManager) activityManager).getRunningAppProcesses();
            for (int iCnt = 0; iCnt < activityes.size(); iCnt++) {
                if (activityes.get(iCnt).processName.equals(processName)) {
                    pids[1] = activityes.get(iCnt).pid;
                }
            }
            if (pids[1] != 0) {
                instance = this;
                SaveText(fileName+".csv", "pressure_pss(MB)"+ "," + processName + "_pss(MB)," + "Active_Memory(MB)"+ "," + "Cached_Memory(MB)" + "," + "Free_Memory(MB)"+ "\n");
                scheduler.scheduleAtFixedRate
                        (new Runnable() {
                            public void run() {
                                // call service
                                Log.i(TAG, "Calling JNI");

                                PassSizeToNative(pressure * 1024 * 1024,repeat);

                                android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
                                try {
                                    Process proc = Runtime.getRuntime().exec("cat /proc/meminfo " + pids[1]);
                                    Process p=Runtime.getRuntime().exec("su");
                                    osw = new OutputStreamWriter(p.getOutputStream());
                                    osw.write("echo -17 > /proc/"+pids[0]+"/oom_adj");
                                    osw.close();
                                    InputStream is = proc.getInputStream();
                                    Map<String, String> memMap = getStringFromInputStream(is);
                                    List<Integer> list=new ArrayList<>();
                                    list.add(memoryInfoArray[0].getTotalPss() / 1024);
                                    list.add(memoryInfoArray[1].getTotalPss() / 1024);
                                    list.add(Integer.parseInt(memMap.get("Active:"))  / 1024);
                                    list.add(Integer.parseInt(memMap.get("Cached:")) / 1024);
                                    list.add(Integer.parseInt(memMap.get("MemFree:")) / 1024);
                                    list.add(Integer.parseInt(memMap.get("MemTotal:")) / 1024);
                                    Log.i(TAG, String.format("** Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n", list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)));
                                    SaveText(fileName+".csv", list.get(0)+ "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "\n");

                                  //  activity.updateClient(list);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, 0, duration, TimeUnit.MILLISECONDS);
                //  timer.scheduleAtFixedRate(new TimerTask() {
                //   public void run() {

//                    Date date=new Date();
//                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//                    try {
//                        Log.i(TAG, String.format("** Time: %d => PSS: %d **\n",date.getTime(), memoryInfoArray[0].getTotalPss()));
//                        myOutWriter.append(dateFormat.format(date)+","+memoryInfoArray[0].getTotalPss() + "\n");
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                //    }

                //  }, delay, period);
            } else {
                Toast.makeText(getApplicationContext(), "Process name : "+processName+" is not running", Toast.LENGTH_SHORT).show();
                onDestroy();

            }
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return mBinder;
    }
    //returns the instance of the service
    public class LocalBinder extends Binder {
        public MySystemService getServiceInstance(){
            return MySystemService.this;
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
    private static Map<String,String> getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        Map<String,String> memoryMap=new HashMap<>();

        try {
            while((line = br.readLine()) != null) {
                String[] strs=line.split(" ");
                memoryMap.put(strs[0],strs[strs.length-2]);
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

        return memoryMap;
    }
    @Override
    public void onDestroy() {
        // timer.purge();
        // timer.cancel();
        scheduler.shutdown();
//        try {
//
//            myOutWriter.close();
//            fOut.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        instance = null;
    }
    public interface Callbacks{
        public void updateClient(List<Integer> data);
    }

}
