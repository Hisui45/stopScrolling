package com.example.stopscrolling;

import android.app.*;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.*;

public class DetectAppService extends Service  {
    private static final String CHANNEL_ID = "stopScrolling";
    private static int LAYOUT_FLAG;
    private boolean popUpActive = false;
    private boolean hasActivatedApps = true;
    private Handler limitHandler,enforceHandler,blockHandler,unblockHandler,timerHandler;
    private boolean keepBlocked, currentlyBlocked;

    private long timerProgress;

    private WindowManager wm;

    public DetectAppService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);
        createNotificationChannel();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification =
                    new Notification.Builder(this, CHANNEL_ID )
                            .setContentTitle(getText(R.string.notification_title))
                            .setContentText(getText(R.string.notification_message))
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentIntent(pendingIntent)
                            .setTicker(getText(R.string.ticker_text))
                            .build();
            //TODO: more professsional
            startForeground(5,notification );
        }


        startDetectLoop();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

         limitHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                if(!popUpActive) {
                    showLimitPopup();
                    popUpActive = true;
                }
            }
        };

        enforceHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                    showEnforcePopup();

            }
        };

        blockHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                blockUser();
            }
        };




      return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showLimitPopup(){

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = li.inflate(R.layout.service_get_limit, null);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    layout.setVisibility(View.INVISIBLE);
                    startDetectLoop();
            }
        });

        layout.findViewById(R.id.oneMBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wm.removeView(layout);
                popUpActive = true;
                Log.d("DetectService", "1 minute btn pressed");
                startTimerTask(1);
            }
        });

        layout.findViewById(R.id.fiveMBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wm.removeView(layout);
                popUpActive = true;
                Log.d("DetectService", "5 minute btn pressed");
                startTimerTask(5);
            }
        });

        layout.findViewById(R.id.tenMBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wm.removeView(layout);
                popUpActive = true;
                Log.d("DetectService", "10 minute btn pressed");
                startTimerTask(10);
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                0,
                0,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        layoutParams.gravity = Gravity.CENTER | Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        wm.addView(layout, layoutParams);
    }

    public void showEnforcePopup(){

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = li.inflate(R.layout.service_enforce_limit, null);


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> activatedApps = getPackageList();
                for(String packageName: activatedApps){
                    if(getTopActivity().equals(packageName)){
                        return;
                    }
                }

                wm.removeView(layout);
                popUpActive = false;
                startDetectLoop();

            }
        });

        layout.findViewById(R.id.oneMBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setVisibility(View.INVISIBLE);
                wm.removeView(layout);
                popUpActive = true;
                Log.d("DetectService", "1 minute btn pressed");
                startTimerTask(1);
            }
        });

        layout.findViewById(R.id.fiveMBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setVisibility(View.INVISIBLE);
                wm.removeView(layout);
                popUpActive = true;
                Log.d("DetectService", "5 minute btn pressed");
                startTimerTask(5);
            }
        });

        layout.findViewById(R.id.tenMBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setVisibility(View.INVISIBLE);
                wm.removeView(layout);
                popUpActive = true;
                Log.d("DetectService", "10 minute btn pressed");
                startTimerTask(10);
            }
        });


        //show me other things to do
        layout.findViewById(R.id.enforceBtn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDetectLoop();

            }
        });
        //close app
        layout.findViewById(R.id.enforceBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DetectService", "Close App Btn Clicked!");
                sendUserToHome();
                wm.removeView(layout);
                startDetectLoop();

            }
        });
        //close and block app
        layout.findViewById(R.id.enforce3Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DetectService", "Close App Btn Clicked!");
                String blockedActivity = getTopActivity();

                sendUserToHome();
                wm.removeView(layout);
                keepBlocked = true;


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        while(keepBlocked){
                            String topActivity = getTopActivity();
                            ArrayList<String> activatedApps = getPackageList();
                            for(String packageName: activatedApps  ){
                                if(topActivity.equals(blockedActivity)){

                                        if(!currentlyBlocked){
                                            Log.d("DetectService", "Blocking User!");
                                            Message message = blockHandler.obtainMessage();
                                            message.sendToTarget();
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }

                                }else{
                                    if(currentlyBlocked) {
                                        Log.d("DetectService", "Unblocking User!");
                                        Message message = unblockHandler.obtainMessage();
                                        message.sendToTarget();
                                    }

                                }
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if(currentlyBlocked) {
                            Log.d("DetectService", "Unblocking User!");
                            Message message = unblockHandler.obtainMessage();
                            message.sendToTarget();
                        }

                        startDetectLoop();

                    }
                }).start();

                long duration = 120000;
                new CountDownTimer(duration,1000){

                    @Override
                    public void onTick(long l) {
                        timerProgress = l;

                    }

                    @Override
                    public void onFinish() {
                        keepBlocked = false;
                    }
                }.start();
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                0,
                0,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        layoutParams.gravity = Gravity.CENTER | Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        wm.addView(layout, layoutParams);
    }


    public void blockUser(){
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View blockLayout = li.inflate(R.layout.service_enforce_block, null);


        blockLayout.findViewById(R.id.enforceBtn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        blockLayout.findViewById(R.id.enforceBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    sendUserToHome();
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                0,
                0,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        layoutParams.gravity = Gravity.CENTER | Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        currentlyBlocked = true;

        unblockHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                wm.removeView(blockLayout);
                currentlyBlocked = false;
            }
        };

        timerHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                long minutes = (timerProgress / 1000)/ 60;
                long seconds = timerProgress / 1000 ;
                if(minutes > 0){
                    if(seconds > 0){
                        ((TextView)blockLayout.findViewById(R.id.blockTimer)).setText(minutes+"m "+(seconds % 60)+"s");

                    }else{
                        ((TextView)blockLayout.findViewById(R.id.blockTimer)).setText(minutes+"m");
                    }

                }else{
                    ((TextView)blockLayout.findViewById(R.id.blockTimer)).setText((seconds % 60)+"s");
                }


            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(currentlyBlocked){
                    Log.d("DetectService", "Timer Updated");
                    Message message = timerHandler.obtainMessage();
                    message.sendToTarget();
                }
            }
        }).start();
        wm.addView(blockLayout, layoutParams);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void checkIfAppOpened(){

        while (!popUpActive && hasActivatedApps) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String topActivity = getTopActivity();

            ArrayList<String> activatedApps = getPackageList();
            if(activatedApps.size() == 0){
                stopForeground(true);
                hasActivatedApps = false;
                stopSelf();

            }else{
                for(String packageName: activatedApps  ){
                    if(topActivity.equals(packageName)){
                        Log.d("DetectService", topActivity+":ProcessOpen");
                        Message message = limitHandler.obtainMessage();
                        message.sendToTarget();
                    }
                }
            }
        }

    }

    public ArrayList<String> getPackageList(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("stopScrolling_preferences"
                , Context.MODE_PRIVATE);

        String[] packageList = {"com.instagram.android", "com.google.android.youtube", "com.zhiliaoapp.musically"};

        ArrayList<String> activatedAppsPackageList = new ArrayList<String>();
        for(String packageName: packageList){
            boolean isActivated = sharedPreferences.getBoolean(packageName,false);
            if(isActivated){
                activatedAppsPackageList.add(packageName);
            }
        }

        return activatedAppsPackageList;

    }

    public void startTimerTask(int duration){

//      long timeToWait = duration * 60000;
        long timeToWait = duration * 2500;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean restartLimit = true;
                    Thread.sleep(timeToWait);
                    ArrayList<String> activatedApps = getPackageList();
                    for(String packageName: activatedApps){
                        if(getTopActivity().equals(packageName)){
                            Message message = enforceHandler.obtainMessage();
                            message.sendToTarget();
                            restartLimit = false;

                        }
                    }

                    if(restartLimit){
                        startDetectLoop();
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }

    public String getTopActivity(){
        String topActivity = "";
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 1000*60;

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);

        if(stats != null)
        {
            SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
            for (UsageStats usageStats : stats)
            {
                mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
            }
            if(mySortedMap != null && !mySortedMap.isEmpty())
            {
                topActivity =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();

            }
        }

        return  topActivity;
    }

    public void startDetectLoop(){
        popUpActive = false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                checkIfAppOpened();
            }
        }).start();
    }

    public void sendUserToHome() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}