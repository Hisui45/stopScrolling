package com.example.stopscrolling.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stopscrolling.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = (RecyclerView) binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new MyAppRecyclerViewAdapter(getAppData(), getContext()));



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//       // recyclerView.getAdapter().notifyDataSetChanged();
//
//    }

    public ArrayList getAppData(){
        ArrayList appData = new ArrayList();
        final PackageManager packageManager = getContext().getPackageManager();
        List<ApplicationInfo> installedApplications =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : installedApplications) {

            if ((appInfo.flags) != 0) {
// seems redundant but reduces load speed
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {

                        Drawable drawable = getContext().getPackageManager().getApplicationIcon(appInfo);
                        String name = (String) getContext().getPackageManager().getApplicationLabel(appInfo);
                        String packageName = appInfo.packageName;
                        if(name.equalsIgnoreCase( "YouTube") || name.equalsIgnoreCase( "Instagram")
                                || name.equalsIgnoreCase( "TikTok") ){
                            AppData data = new AppData(drawable, name, packageName);
                            appData.add(data);

                    }
                } else {
                        Drawable drawable = getContext().getPackageManager().getApplicationIcon(appInfo);
                        String name = (String) getContext().getPackageManager().getApplicationLabel(appInfo);
                        String packageName = appInfo.packageName;
                        if(name.equalsIgnoreCase( "YouTube") || name.equalsIgnoreCase( "Instagram")
                                || name.equalsIgnoreCase( "TikTok") ){
                            AppData data = new AppData(drawable, name, packageName);
                            appData.add(data);
                    }
                }



            }
        }

        return  appData;
    }

    public Object[] getAppPreferences(){
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet("activated_apps", null).toArray();
    }
}