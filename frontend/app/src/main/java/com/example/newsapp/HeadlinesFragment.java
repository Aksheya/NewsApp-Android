package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class HeadlinesFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.headlines_fragment, container, false);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.headlines_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.world_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.business_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.politics_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.sports_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.technology_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.science_tab));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = view.findViewById(R.id.headlines_pager);
        viewPager.setAdapter(new TabAdapter(getFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }
}
