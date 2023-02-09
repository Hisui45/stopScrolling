package com.example.stopscrolling;

import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;


public class UpdateStats extends Application {

    ExecutorService executorService;


    ArrayList<AppStats> appStatsList = new ArrayList<AppStats>();

    public String makeStatsRequest(Context context) throws ExecutionException, InterruptedException {
        Runnable statsRequestRun = new Runnable() {
            @Override
            public void run() {
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = ((MainActivity)context);
                    boolean hasPermission = mainActivity.checkForUsagePermission(context);


                    if(hasPermission) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        long start = calendar.getTimeInMillis();
                        long end = System.currentTimeMillis();
                        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                        List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
                        for (UsageStats stats: statsList) {

                            if(stats.getPackageName().equalsIgnoreCase("com.google.android.youtube")
                                    || stats.getPackageName().equalsIgnoreCase( "com.instagram.android")
                                    || stats.getPackageName().equalsIgnoreCase( "com.zhiliaoapp.musically")){


                                appStatsList.add(new AppStats(stats.getPackageName(), stats.getTotalTimeInForeground()));
                            }

                        }


                        //     appStats.setText(appStatsList.get(0).getPackageName());
                    }else{

                        mainActivity.sendUserToUsageSettings();
                    }
                }

            }
        };

        Future<String>  result = executorService.submit(statsRequestRun, "DONE");

        while(result.isDone() == false){
            try{
                return result.get();
            }catch (InterruptedException | ExecutionException e){
                return e.toString();
            }
        }

        return null;

    }


    public UpdateStats(ExecutorService executorService){
        this.executorService = executorService;


    }

    public ArrayList<AppStats> getAppStatsList() {
        return appStatsList;
    }

}
