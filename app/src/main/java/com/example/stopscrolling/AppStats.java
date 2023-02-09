package com.example.stopscrolling;

public class AppStats {

    private String packageName;
   // private int timesOpened;
    private long timeInForeground;

    public AppStats(String packageName, long timeInForeground) {
        this.packageName = packageName;
       // this.timesOpened = timesOpened;
        this.timeInForeground = timeInForeground;
    }

    public String getPackageName() {

        if(packageName.equalsIgnoreCase("com.google.android.youtube")){
            return "YouTube";

        }else if(packageName.equalsIgnoreCase( "com.instagram.android")){

            return "Instagram";
        }else if(packageName.equalsIgnoreCase( "com.zhiliaoapp.musically")){

            return "TikTok";
        }

        return null;
    }

//    public int getTimesOpened() {
//        return timesOpened;
//    }

    public String getTimeInForegroundInMinutes() {
        int minutes = (int)(timeInForeground / 60000);
        int hours = 0;
        while (minutes >= 60){
            hours++;
            minutes = minutes - 60;
        }

        if(hours > 0){
            return hours+"h"+minutes+"m";
        }
        return minutes+"m";
    }



}
