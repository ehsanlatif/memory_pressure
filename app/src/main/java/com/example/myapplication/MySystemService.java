package com.example.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import android.support.annotation.FontRes;
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

    public String memoryStat(int level) {
        //     super.onTrimMemory(level);
        //  }
        //  public void memoryStat(int level) {

        // Determine which lifecycle or system event was raised.
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

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

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
                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */


            default:
                return "same";
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
        }
    }
    public native int PassSizeToNative(int a,boolean b);

    private static MySystemService instance = null;
    private final IBinder mBinder = new LocalBinder();
    String fileName;
    int duration;
    boolean repeat;
    String processName;
    int pressure;
    int init_pressure;
    Callbacks activity;
    private static int initial_Cache;


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
    myAsyncTask myAsyncTask;
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public IBinder onBind(final Intent intent) {
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
                //String processName = process.name;
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
            myAsyncTask=new myAsyncTask(activityManager);
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
                SaveText(fileName+".csv", "time,"+"pressure_pss(MB)"+ "," + processName + "_pss(MB)," + "Active_Memory(MB)"+ "," + "Cached_Memory(MB)" + "," + "Free_Memory(MB)"+","+"VM_Pressure"+"\n");
                if(repeat==true) {
                  //  scheduler.scheduleWithFixedDelay
                        //    (new Runnable() {
                        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        //        public void run() {
                                    // call service
                    String[] cmd = {"su", "-c", "echo -17 > /proc/" + pids[0] + "/oom_adj"};
                    String[] cmd1 = {"su", "-c", "echo -1000 > /proc/" + pids[0] + "/oom_score_adj"};
                    try {
                        Runtime.getRuntime().exec(cmd);
                        Runtime.getRuntime().exec(cmd1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myAsyncTask.execute(duration);
//                    while(true) {
//                        Log.i(TAG, "Calling JNI");
//
//                        String[] cmd = {"su", "-c", "echo -17 > /proc/" + pids[0] + "/oom_adj"};
//                        String[] cmd1 = {"su", "-c", "echo -1000 > /proc/" + pids[0] + "/oom_score_adj"};
//                        try {
//                            Runtime.getRuntime().exec(cmd);
//                            Runtime.getRuntime().exec(cmd1);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        //android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
//
//                        List<Integer> list = findMemoryStats(activityManager);
//
//                        if (first_run)
//                            initial_Cache = list.get(3) + list.get(4);
//
//                        if ((list.get(3) + list.get(4)) > initial_Cache)
//                            initial_Cache = list.get(3) + list.get(4);
//                        double vmPressure = (double) (((initial_Cache - (list.get(3) + list.get(4))) * 100) / initial_Cache);
//                        Log.w(TAG, "VM_Pressure: " + vmPressure + "%");
//
//
//                        Date date = new Date();
//                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//                        Log.i(TAG, String.format("**** Time: %s ==> Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n", dateFormat.format(date), list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
//                        SaveText(fileName + ".csv", dateFormat.format(date) + "," + list.get(0) + "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "," + vmPressure + "\n");
//
//                        if (first_run == true)
//                            PassSizeToNative(init_pressure * 1024 * 1024, repeat);
//                        else
//                            PassSizeToNative(pressure * 1024 * 1024, repeat);
//
//                        first_run = false;
//                        try {
//                            Thread.sleep(duration);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }


                               // }
                          //  }, 0, duration, TimeUnit.MILLISECONDS);
                }else
                {


                    String[] cmd = {"su", "-c", "echo -17 > /proc/" + pids[0] + "/oom_adj"};
                    String[] cmd1 = {"su", "-c", "echo -1000 > /proc/" + pids[0] + "/oom_score_adj"};
                    try {
                        Runtime.getRuntime().exec(cmd);
                        Runtime.getRuntime().exec(cmd1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    List<Integer> list=findMemoryStats(activityManager);

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

                    List<Integer> list2=findMemoryStats(activityManager);

                    double newVmPressure = (double) (((initial_Cache - (list2.get(3) + list2.get(4))) * 100) / initial_Cache);
                    Log.w(TAG, "VM_Pressure: " + newVmPressure + "%");


                    date = new Date();
                    Log.i(TAG, String.format("**** Time: %s ==> Pressure: %d => PSS: %d => Active: %d => Cached: %d => Free: %d **\n", dateFormat.format(date), list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
                    SaveText(fileName + ".csv", dateFormat.format(date) + "," + list.get(0) + "," + list.get(1) + "," + list.get(2) + "," + list.get(3) + "," + list.get(4) + "," + newVmPressure + "\n");


                }
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
    public class  myAsyncTask extends AsyncTask<Integer,Void,Void>
    {
        ActivityManager activityManager;
        myAsyncTask(ActivityManager activityManager)
        {
            this.activityManager=activityManager;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Void doInBackground(Integer... integers) {
            while(true) {
                Log.i(TAG, "Calling JNI");

                String[] cmd = {"su", "-c", "echo -17 > /proc/" + pids[0] + "/oom_adj"};
                String[] cmd1 = {"su", "-c", "echo -1000 > /proc/" + pids[0] + "/oom_score_adj"};
                try {
                    Runtime.getRuntime().exec(cmd);
                    Runtime.getRuntime().exec(cmd1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);

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
           // return null;
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
        // timer.purge();
        // timer.cancel();
        //scheduler.shutdown();
        myAsyncTask.cancel(true);
        PassSizeToNative(0,false);

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
