package com.example.myapplication;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

public class MainActivity extends AppCompatActivity implements MySystemService.Callbacks{

    // Used to load the 'native-lib' library on application startup.

    private static boolean started=false;
    TextInputLayout period_layout;
    TextInputEditText pressure,process_name,period,output;
    RadioGroup radioGroup;
    TextView total_memory,process_pressure,total_pressure,process_pss,free_memory,repeat;
    Intent serviceIntent;
    MySystemService myService;
    Switch aSwitch;
     Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        serviceIntent = new Intent(MainActivity.this, MySystemService.class);
        Bundle extras=getIntent().getExtras();

        if(extras!=null && extras.getInt("pressure",0)!=0)
        {
            Toast.makeText(getApplicationContext(),extras.toString(),Toast.LENGTH_LONG).show();
            int pres=extras.getInt("pressure",0);
            String proc=extras.getString("proc_name");
            int per=extras.getInt("period",0);
            int init_pers=extras.getInt("initial_pressure",0);
            String output_file=extras.getString("output");
            serviceIntent.putExtra("filename", output_file);
            serviceIntent.putExtra("duration", per);
            serviceIntent.putExtra("repeat", per==0?false:true);
            serviceIntent.putExtra("process", proc);
            serviceIntent.putExtra("pressure", pres);
            serviceIntent.putExtra("initial_pressure", init_pers);

            Toast.makeText(getApplicationContext(),pres + " , "+proc+" , "+per+" , "+output_file,Toast.LENGTH_LONG).show();
            //  startService(serviceIntent); //Starting the service
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            //textView.setText("Service is Running!");
//            /button.setText("Stop");
//            started=true;
        }

        button=findViewById(R.id.button);
        period_layout=findViewById(R.id.textInputLayout3);
        pressure=findViewById(R.id.pressure);
        process_name=findViewById(R.id.package_name);
        period=findViewById(R.id.period);
        output=findViewById(R.id.out_directory);
        radioGroup=findViewById(R.id.group);
        aSwitch=findViewById(R.id.switch1);
        repeat=findViewById(R.id.textView4);
//        process_pressure=findViewById(R.id.process_pressure);
//        total_pressure=findViewById(R.id.total_pressure);
//        process_pss=findViewById(R.id.process_pss);
//        free_memory=findViewById(R.id.free_memory);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    radioGroup.setVisibility(View.GONE);
                    repeat.setVisibility(View.GONE);
                    aSwitch.setText("%");
                }
                else {
                    radioGroup.setVisibility(View.VISIBLE);
                    repeat.setVisibility(View.VISIBLE);
                    aSwitch.setText("MB");

                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.repeat_yes)
                {
                    period_layout.setVisibility(View.VISIBLE);
                }else
                {
                    period_layout.setVisibility(View.GONE);

                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!started)
                {


                    if(!MySystemService.isInstanceCreated()) {
                        if(!(output.getText().toString().isEmpty() || process_name.getText().toString().isEmpty() || pressure.getText().toString().isEmpty())) {
                            if(aSwitch.isChecked()){
                                serviceIntent.putExtra("filename", output.getText().toString());
                                serviceIntent.putExtra("process", process_name.getText().toString());
                                serviceIntent.putExtra("pressure", Integer.parseInt(pressure.getText().toString()));
                                serviceIntent.putExtra("repeat", false);
                                serviceIntent.putExtra("duration",  1000);
                                serviceIntent.putExtra("initial_pressure", -1);
                                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
                                //textView.setText("Service is Running!");
                                button.setText("Stop");
                                started = true;

                            }else {
                                boolean repeat = radioGroup.getCheckedRadioButtonId() == R.id.repeat_yes;
                                if (!(repeat && period.getText().toString().isEmpty())) {
                                    serviceIntent.putExtra("filename", output.getText().toString());
                                    serviceIntent.putExtra("duration", repeat ? Integer.parseInt(period.getText().toString()) : 1000);
                                    serviceIntent.putExtra("repeat", repeat);
                                    serviceIntent.putExtra("process", process_name.getText().toString());
                                    serviceIntent.putExtra("pressure", Integer.parseInt(pressure.getText().toString()));
                                    serviceIntent.putExtra("initial_pressure", Integer.parseInt(pressure.getText().toString()));


                                    //  startService(serviceIntent); //Starting the service
                                    bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
                                    //textView.setText("Service is Running!");
                                    button.setText("Stop");
                                    started = true;
                                } else
                                    Toast.makeText(getApplicationContext(), "Enter repetition period in ms", Toast.LENGTH_SHORT).show();
                            }
                        }else
                            Toast.makeText(getApplicationContext(),"Fill all required Fields",Toast.LENGTH_SHORT).show();
                    }

                }else {


                    if(MySystemService.isInstanceCreated()) {
                        unbindService(mConnection);
                       // stopService(serviceIntent);
                        //textView.setText("No Service is Running! ");
                        started=false;
                        button.setText("Start");
                    }

                }
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            MySystemService.LocalBinder binder = (MySystemService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void updateClient(List<Integer> data) {
    total_memory.setText(data.get(5)+"MB");
    process_pressure.setText(data.get(0)+"MB");
    total_pressure.setText(data.get(2)+"MB");
    process_pss.setText(data.get(1)+"MB");
    free_memory.setText(data.get(4)+"MB");
    }
}
