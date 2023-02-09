package com.example.stopscrolling;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteListenableWorker;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class TimerWorker extends ListenableWorker {

    Context appContext;
    WorkerParameters workerParams;
    String[] packageList = {"com.instagram.android", "com.google.android.youtube", "com.zhiliaoapp.musically"};
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public TimerWorker(@NonNull @NotNull Context appContext, @NonNull @NotNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.appContext = appContext;
        this.workerParams = workerParams;
    }

    @NonNull
    @NotNull
    @Override
    public ListenableFuture<Result> startWork() {

            ActivityManager manager = (ActivityManager) this.appContext.getSystemService(Context.ACTIVITY_SERVICE);
            List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

            ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
            if(componentInfo.getPackageName().equals(packageList[0])) {
                return (ListenableFuture<Result>) Result.success();
            }

        return (ListenableFuture<Result>) Result.retry();
    }


}
