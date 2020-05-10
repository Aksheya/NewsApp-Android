package com.example.newsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public void getHomeFragmentData(String url, Context context, final CallBack callback) {
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        List<Card> cardItems = new ArrayList<Card>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject res = response.getJSONObject(i);
                                Card card = new Card(res.getString("section"),
                                        res.getString("time"),
                                        res.getString("title"),
                                        res.getString("articleId"),
                                        res.getString("shareUrl"),
                                        res.getString("image"));
                                cardItems.add(card);
                            }
                            Log.d("handle", "dfdf");
                            callback.dataLoaded(cardItems);
                        } catch (JSONException error) {
                            Log.d("MYERROR0", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("MYERROR", error.toString());
                    }
                });

        queue.add(jsonArrayRequest);
    }

    static String handleBookmark(Card card, Context context) {
        String uri;
        Gson gson = new Gson();
        SharedPreferences pref = context.getSharedPreferences("BookmarkPref", 0); // 0 - for private mode
        String cardString = gson.toJson(card);
        SharedPreferences.Editor prefsEditor = pref.edit();
        String objectstring = pref.getString(card.articleId, null);
        if (objectstring != null) {
            prefsEditor.remove(card.articleId);
            prefsEditor.commit();
            uri = "@drawable/ic_bookmark_border_black_24dp";
            Toast toast = Toast.makeText(context, "\"" + card.title + "\" was removed from bookmarks", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            prefsEditor.putString(card.articleId, cardString);
            prefsEditor.commit();
            uri = "@drawable/ic_bookmark_black_24dp";
            Toast toast = Toast.makeText(context, "\"" + card.title + "\" was added to bookmarks", Toast.LENGTH_SHORT);
            toast.show();
        }
        return uri;
    }

    void implementSwipe(final String url, final Context context, View view) {
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.homeFragmentCard);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHomeFragmentData(url, context, new Utility.CallBack() {
                    @Override
                    public void dataLoaded(List<Card> cards) {
                        recyclerView.setAdapter(new CardAdapter(cards, "MainActivity", "listLayout", null));
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
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });
    }

    public static void setProgressBar(View view, boolean set) {
        RelativeLayout progressBar = (RelativeLayout) view.findViewById(R.id.includedProgressBar);
        if (set) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }


    public interface CallBack {
        void dataLoaded(List<Card> detailsMovies);

        void dataError(String msg);
    }
}
