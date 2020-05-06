package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabAdapter extends FragmentStatePagerAdapter {
    int tabCount;
    public TabAdapter(FragmentManager fragmentManager, int tabCount){
        super(fragmentManager,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.tabCount = tabCount;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new World();
            case 1: return new Business();
            case 2: return new Politics();
            case 3: return new Sports();
            case 4: return new Technology();
            case 5: return new Science();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
