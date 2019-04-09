package com.example.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;

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
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class MySystemService extends Service {
    static {
        System.loadLibrary("native-lib");
    }

    public void memoryStat(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE: {
                Log.d(TAG, "on Moderat Pressure");
                SaveText(fileName+".csv", "Moderat Pressure, , , , \n");
            }
            break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW: {
                Log.d(TAG, "on High Pressure");
                SaveText(fileName+".csv", "High Pressure, , , , \n");
            }
            break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:{
                Log.d(TAG,"on Critical Pressure");
                SaveText(fileName+".csv", "Critical Pressure, , , , \n");
            }
            break;

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            {
                Log.d(TAG, "on Moderat Pressure");
                SaveText(fileName+".csv", "Background Pressure, , , , \n");
            }
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            {
                Log.d(TAG, "on Moderat Pressure");
                SaveText(fileName+".csv", "Moderat Pressure, , , , \n");
            }
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }
    public native int PassSizeToNative(String proc_name,int a,boolean b);

    private static MySystemService instance = null;
    private final IBinder mBinder = new LocalBinder();
    String fileName;
    int duration;
    boolean repeat;
    String processName;
    int pressure;
    int init_pressure;
    Callbacks activity;


    Timer timer = new Timer();
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
    double last_seconds=-1;
    boolean first_run=false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(),"Service Started",Toast.LENGTH_SHORT).show();
        if(intent.getExtras()!=null) {
            fileName = intent.getStringExtra("filename");
            duration = intent.getIntExtra("duration", 0);
            repeat = intent.getBooleanExtra("repeat", false);
            processName = intent.getStringExtra("process");
            pressure = intent.getIntExtra("pressure", 0);
            init_pressure = intent.getIntExtra("initial_pressure", 0);
        }
        pids[0] = android.os.Process.myPid();

        try {
           // pids[1]=PassSizeToNative("com.google.android.gm.lite",-1,repeat);


            List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
            final ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
            if(activityManager.isLowRamDevice())
                Toast.makeText(getApplicationContext(),"Is LowDevice",Toast.LENGTH_LONG).show();

            for (AndroidAppProcess process : processes) {
                // Get some information about the process
                String processName = process.name;
//
//                Stat stat = process.stat();
               // int pid = stat.getPid();
                if (process.name.equals(processName)) {
                    pids[1] = process.stat().getPid();
                    break;
                }
//                int parentProcessId = stat.ppid();
//                long startTime = stat.stime();
//                int policy = stat.policy();
//                char state = stat.state();
//
//                Statm statm = process.statm();
//                long totalSizeOfProcess = statm.getSize();
//                long residentSetSize = statm.getResidentSetSize();
//
//                PackageInfo packageInfo = process.getPackageInfo(context, 0);
//                String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
//            }
//            List<ActivityManager.RunningAppProcessInfo> actprocivityes = ((ActivityManager)activityManager).getR();
//
//            for (int iCnt = 0; iCnt < activityes.size(); iCnt++){
//
//                System.out.println("APP: "+iCnt +" "+ activityes.get(iCnt).processName);
//
//                if (activityes.get(iCnt).processName.contains(processName)){
////                    android.os.Process.sendSignal(activityes.get(iCnt).pid, android.os.Process.SIGNAL_KILL);
////                    android.os.Process.killProcess(activityes.get(i).pid);
//                    pids[1]=activityes.get(iCnt).pid;
//                    //manager.killBackgroundProcesses("com.android.email");
//
//                    //manager.restartPackage("com.android.email");
//
//                    System.out.println("Inside if");
//                }
//
            }
            if(pids[1]==0) {
                String[] cmd = {"su", "-c", "pidof " + processName};
                Process proc2 = Runtime.getRuntime().exec(cmd);
                InputStream upis = proc2.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(upis));
                pids[1] = Integer.parseInt(br.readLine().toString());
            }
//            final ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
//            String currentApp = "NULL";
////            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
////                UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
////                long time = System.currentTimeMillis();
////                List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
////                if (appList != null && appList.size() > 0) {
////                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
////                    for (UsageStats usageStats : appList) {
////                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
////                    }
////                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
////                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
////                    }
////                } else {
//                    List<ActivityManager.RunningServiceInfo> activityes = ((ActivityManager) activityManager).getRunningServices(Integer.MAX_VALUE);
//                    for (int iCnt = 0; iCnt < activityes.size(); iCnt++) {
//                        if (activityes.get(iCnt).process.equals(processName)) {
//                            pids[1] = activityes.get(iCnt).pid;
//                        }
//                    }
            // }
            // }
            first_run=true;
            if (processName==null || pids[1] != 0) {
                instance = this;
                SaveText(fileName+".csv", "time,"+"pressure_pss(MB)"+ "," + processName + "_pss(MB)," + "Active_Memory(MB)"+ "," + "Cached_Memory(MB)" + "," + "Free_Memory(MB)"+ "\n");
                scheduler.scheduleAtFixedRate
                        (new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            public void run() {
                                // call service
                                Log.i(TAG, "Calling JNI");

                                if(first_run)
                                    PassSizeToNative("",init_pressure * 1024 * 1024,repeat);
                                else
                                    PassSizeToNative("",pressure * 1024 * 1024,repeat);
                                first_run=false;

                                android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
                                try {
                                    Process proc = Runtime.getRuntime().exec("cat /proc/meminfo " + pids[1]);
//                                    Process proc1 = Runtime.getRuntime().exec("cat /proc/" + 20 + "/stat");
//                                    Process proc2 = Runtime.getRuntime().exec("cat /proc/uptime");
//                                    InputStream upis = proc2.getInputStream();
//                                    BufferedReader br = new BufferedReader(new InputStreamReader(upis));
//                                    double uptime = Double.parseDouble(br.readLine().split(" ")[0]);

                                    Process p = Runtime.getRuntime().exec("su");
                                    osw = new OutputStreamWriter(p.getOutputStream());
                                    osw.write("echo -17 > /proc/" + pids[0] + "/oom_adj");
                                    osw.close();
                                    InputStream is = proc.getInputStream();
                                    Map<String, Integer> memMap = getStringFromInputStream(is, 2);
                                    ActivityManager.RunningAppProcessInfo rapI=new ActivityManager.RunningAppProcessInfo();
                                    activityManager.getMyMemoryState(rapI);
                                    memoryStat(rapI.lastTrimLevel);
                                    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                                    activityManager.getMemoryInfo(memoryInfo);
                                    Log.w(TAG,"Total Memory: "+ memoryInfo.totalMem+" => Memory in use : "+memoryInfo.availMem);
                                    if(memoryInfo.lowMemory) {
                                        Log.e(TAG, "low memory and threshold:" + memoryInfo.threshold);
                                        SaveText(fileName+".csv", "Critical Pressure, , , , \n");

                                    }
                                    //InputStream is1 = proc1.getInputStream();
//                                    Map<String, Integer> cpuMap = getStringFromInputStream(is1, true);
//                                    int utime = cpuMap.get("utime");
//                                    int stime = cpuMap.get("stime");
//                                    int cutime = cpuMap.get("cutime");
//                                    int cstime = cpuMap.get("cstime");
//                                    int starttime = cpuMap.get("starttime");
//                                    int total_time = utime + stime;
//                                    //int Hertz=0;
//                                    total_time = total_time + cutime + cstime;
                                    //double seconds = uptime - (starttime / Hertz);
//                                    double cpu_usage;
//                                    if (last_seconds == -1){
//                                        cpu_usage = total_time;
//                                        last_seconds = total_time;
//                                    }
//                                    else
//                                    {
//                                        cpu_usage=total_time-last_seconds;//total_time / Hertz;// 100 * ((total_time / Hertz) / seconds);
//                                        last_seconds=total_time;
//
//                                    }

                                    List<Integer> list=new ArrayList<>();
                                    list.add(memoryInfoArray[0].getTotalPss() / 1024);
                                    list.add(memoryInfoArray[1].getTotalPss() / 1024);
                                    list.add(memMap.get("Active:") / 1024);
                                    list.add(memMap.get("Cached:") / 1024);
                                    list.add(memMap.get("MemFree:") / 1024);
                                    list.add(memMap.get("MemTotal:") / 1024);
                                    Date date=new Date();
                                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                    Log.i(TAG, String.format("**** Time: %s ==> Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n",dateFormat.format(date), list.get(0),list.get(1),list.get(2),list.get(3),list.get(4)));
                                    SaveText(fileName+".csv", dateFormat.format(date)+","+list.get(0)+ "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "\n");

                                    //  activity.updateClient(list);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, 0, duration, TimeUnit.MILLISECONDS);
//                  timer.scheduleAtFixedRate(new TimerTask() {
//                   public void run() {
//
//                    Date date=new Date();
//                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//                    android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
//                        Log.i(TAG, String.format("** Time: %s => PSS: %.3f **\n",dateFormat.format(date), ((double)memoryInfoArray[1].getTotalPss()/1024)));
//                         SaveText(fileName+".csv", dateFormat.format(date)+","+memoryInfoArray[1].getTotalPss()+ "\n");
//                    }
//
//                  }, 0, 10);
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
        // timer.purge();
        // timer.cancel();
        scheduler.shutdown();
        //PassSizeToNative(0,false);
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
