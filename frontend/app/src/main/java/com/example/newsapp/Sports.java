package com.example.newsapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class Sports extends Fragment {
    RecyclerView recyclerView;
    private  List<Card> cardList;
    public Sports() {
        // Required empty public constructor
    }



    @Override
    public void onResume(){
        super.onResume();
        if(cardList != null)
            recyclerView.setAdapter(new CardAdapter(cardList, "MainActivity", "listLayout",null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sports_tab, container, false);
        Context context = view.getContext();
        Utility.setHeadlinesProgressBar(view,true);
        recyclerView = (RecyclerView) view.findViewById(R.id.homeFragmentCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        String url = "https://android-newapp-aks05.wl.r.appspot.com/sports_fragment";
        Utility util = new Utility();
        util.getHomeFragmentData(url, getActivity(), new Utility.CallBack() {
            @Override
            public void dataLoaded(List<Card> cards) {
                cardList = cards;
                recyclerView.setAdapter(new CardAdapter(cards, "MainActivity", "listLayout", null));
                Utility.setHeadlinesProgressBar(view,false);
            }

            @Override
            public void dataError(String msg) {
                Log.d("ErrorMessage", msg);
            }
        });
        util.implementSwipe(url, context, view);
        return view;
    }
}
