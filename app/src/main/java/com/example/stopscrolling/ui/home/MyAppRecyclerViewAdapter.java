package com.example.stopscrolling.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.Switch;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stopscrolling.AppStats;
import com.example.stopscrolling.MainActivity;
import com.example.stopscrolling.UpdateStats;
import com.example.stopscrolling.databinding.FragmentAppBinding;
import com.example.stopscrolling.ui.home.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAppRecyclerViewAdapter extends RecyclerView.Adapter<MyAppRecyclerViewAdapter.ViewHolder> {

    private final List<AppData> mValues;

    private Context mainContext;
    private MainActivity mainActivity;
    private AppPreferences appPreferences;
    public MyAppRecyclerViewAdapter(List<AppData> items, Context context) {
        mValues = items;
        mainContext = context;
        if (mainContext instanceof MainActivity) {
            mainActivity = ((MainActivity) mainContext);
        }
        appPreferences = new AppPreferences();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setImageDrawable(mValues.get(position).getICON());
        holder.mContentView.setText(mValues.get(position).getNAME());

        boolean isActivated = appPreferences.getPreference(holder.mItem.getPackageName());

        if(isActivated){
            holder.appSwitch.setChecked(true);
            holder.appStats.setVisibility(View.VISIBLE);
            holder.appStats.setText(updateStats(holder.mContentView.getText().toString()));
            mainActivity.increaseNumberOfActivatedApps();
            startDisplayService(holder.mItem.getPackageName());

        }else{

            holder.appSwitch.setChecked(false);
            holder.appStats.setVisibility(View.INVISIBLE);
        }
//TODO: fully check if there's the permission in all cases
            boolean hasPermission = mainActivity.checkForUsagePermission(mainContext);
            if(!hasPermission) {
                holder.appSwitch.setChecked(false);
                holder.mItem.setActive(false);
            }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mIdView;
        public final TextView mContentView;
        public AppData mItem;
        public final CardView cardView;
        public final Switch appSwitch;
        public final TextView appStats;

        public ViewHolder(FragmentAppBinding binding) {
            super(binding.getRoot());
            mIdView = binding.icon;
            mContentView = binding.appName;
            cardView = binding.cardView;
            appSwitch = binding.appSwitch;
            appStats = binding.appStats;


            appSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isActivated = appPreferences.getPreference(mItem.getPackageName());
                    if(isActivated){
                        appStats.setVisibility(View.INVISIBLE);
                        appPreferences.setPreference(false, mItem.getPackageName());
                        mainActivity.decreaseNumberOfActivatedApps();
                    }else{
                        appStats.setVisibility(View.VISIBLE);
                        appStats.setText(updateStats(mContentView.getText().toString()));
                        mainActivity.increaseNumberOfActivatedApps();
                        startDisplayService(mItem.getPackageName());

                    }
                }
            });
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public void startDisplayService(String packageName){
        boolean hasPermission = mainActivity.checkForDisplayPermission(mainContext);

        if (hasPermission) {
            appPreferences.setPreference(true, packageName);

                mainActivity.startDetectAppService();

        }else{
            mainActivity.sendUserToDisplaySettings();
        }
    }

    public String updateStats(String packageName){
        ArrayList<AppStats> stats;
        try {
            stats = makeStatsRequest();
            for (AppStats appStat: stats) {
                if(packageName.equalsIgnoreCase(appStat.getPackageName())){

                    return appStat.getTimeInForegroundInMinutes();
                }
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return "null";
    }

    public ArrayList makeStatsRequest() throws ExecutionException, InterruptedException {

        UpdateStats updateStats = new UpdateStats(mainActivity.getExecutorService());
        String result = updateStats.makeStatsRequest(mainContext);
           if(result == "DONE"){
               return updateStats.getAppStatsList();
           }
        return null;
    }
    private class AppPreferences{
        SharedPreferences sharedPreferences = mainContext.getSharedPreferences("stopScrolling_preferences"
                , Context.MODE_PRIVATE);


        public void setPreference(boolean isActivated, String packageName){
            sharedPreferences.edit().putBoolean(packageName, isActivated).commit();
        }

        public boolean getPreference(String packageName){
            return sharedPreferences.getBoolean(packageName, false);
        }
    }



}

