package com.example.newsapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookmarkFragment extends Fragment implements OnBookmarkClickListener {
    private RecyclerView recyclerView;
    private List<Card> cardList;
    private TextView textView;
    private Context context;

    public BookmarkFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmark_fragment, container, false);
        textView = (TextView) view.findViewById(R.id.emptyBookmark);
        context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.bookmarkFragmentCard);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        cardList = getAllItems(context);
        if (cardList.size()>0){
            recyclerView.setAdapter(new CardAdapter(cardList, "MainActivity", "gridLayout", this));
        }
        else {
            textView.setText("No Bookmarked Articles");
        }
        return view;
    }

    private List<Card> getAllItems(Context context) {
        Gson gson = new Gson();
        SharedPreferences pref = context.getSharedPreferences("BookmarkPref", 0);
        Map<String, ?> allBookmarkObjects = pref.getAll();
        String objectString = "";
        List<Card> cards = new ArrayList<Card>();
        for (Map.Entry<String, ?> entry : allBookmarkObjects.entrySet()) {
            objectString = pref.getString(entry.getKey(), null);
            Card card = gson.fromJson(objectString, Card.class);
            cards.add(card);
        }
        return cards;
    }

    @Override
    public void onResume() {
        super.onResume();
        cardList = getAllItems(context);
        if (cardList != null || cardList.size()>0){
            if(cardList.size()>0)
                textView.setText("");
            else{
                textView.setText("No Bookmarked Articles");
            }
            recyclerView.setAdapter(new CardAdapter(cardList, "MainActivity", "gridLayout", this));
        }
        else {
            Log.d("FD","fd");
            textView.setText("No Bookmarked Articles");
        }
    }

    @Override
    public void onBookmarkClick(int position) {
        Log.d("position", String.valueOf(position));
        cardList.remove(position);
        if (cardList.size()>0){
            textView.setText("");
            recyclerView.setAdapter(new CardAdapter(cardList, "MainActivity", "gridLayout", this));
        }
        else {
            textView.setText("No Bookmarked Articles");
        }
    }
}
