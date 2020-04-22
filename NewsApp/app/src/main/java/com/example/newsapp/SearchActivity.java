package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;
import java.util.List;

import static com.example.newsapp.MainActivity.EXTRA_MESSAGE;

public class SearchActivity extends AppCompatActivity {
    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private List<Card> cardList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (savedInstanceState != null)
            return;
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myMenuBar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        final Intent intent = getIntent();
        String keyword = intent.getExtras().getString("keyword");
        processSearch(keyword);
        ab.setTitle("Search Results for " + intent.getExtras().getString("keyword"));
        mSwipeRefreshLayout = this.findViewById(R.id.swiperefresh_items);
        final Context that = this;
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                String url = "http://10.0.2.2:8080/search?keyword=" + intent.getExtras().getString("keyword");
                Utility util = new Utility();
                util.getHomeFragmentData(url, that, new Utility.CallBack() {
                    @Override
                    public void dataLoaded(List<Card> cards) {
                        cardList = cards;
                        recyclerView.setAdapter(new CardAdapter(cards, "SearchActivity", "listLayout", null));
                    }

                    @Override
                    public void dataError(String msg) {
                        Log.d("ErrorMessage", msg);
                    }
                });


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });
    }
    public void processSearch(String query) {
        Utility.setHeadlinesProgressBar(getWindow().getDecorView().findViewById(android.R.id.content),true);
        String url = "http://10.0.2.2:8080/search?keyword=" + query;
        Utility util = new Utility();
        final Context that = this;
        util.getHomeFragmentData(url, this, new Utility.CallBack() {
            @Override
            public void dataLoaded(List<Card> cards) {
                cardList = cards;
                recyclerView = (RecyclerView) findViewById(R.id.search_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(that));
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
                recyclerView.setAdapter(new CardAdapter(cardList, "SearchActivity", "listLayout",null));
                        Utility.setHeadlinesProgressBar(getWindow().getDecorView().findViewById(android.R.id.content),false);

            }

            @Override
            public void dataError(String msg) {
                Log.d("ErrorMessage", msg);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(cardList != null)
            recyclerView.setAdapter(new CardAdapter(cardList, "SearchActivity", "listLayout",null));
    }
}
