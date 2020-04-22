package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import android.app.SearchManager;
import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "searchList";
    private RequestQueue queue;
    private SearchView.OnQueryTextListener queryTextListener;
    private SearchView searchView;
    private AutoSuggestAdapter autoSuggestAdapter;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        setProgressBarIndeterminateVisibility(true);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.fragment_bottom_container) != null) {
            if (savedInstanceState != null)
                return;
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myMenuBar);
        setSupportActionBar(myToolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_tab);
        bottomNav.setOnNavigationItemSelectedListener(bottomTabListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_bottom_container, new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomTabListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedTab = null;
                    switch (item.getItemId()) {
                        case R.id.home:
                            selectedTab = new HomeFragment();
                            break;
                        case R.id.headlines:
                            selectedTab = new HeadlinesFragment();
                            break;
                        case R.id.trending:
                            selectedTab = new TrendingFragment();
                            break;
                        case R.id.bookmark:
                            selectedTab = new BookmarkFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_bottom_container, selectedTab).commit();
                    return true;
                }
            };

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        final SearchView searchV = searchView;
        final SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString = (String) adapterView.getItemAtPosition(itemIndex);
                autoCompleteTextView.setText("" + queryString);
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        String text = autoCompleteTextView.getText().toString();
                        if (text.length() >= 3) {
                            callBingAutosuggest(text);
                        }
                    }
                }
                return false;
            }
        });
        final Context that = this;
        queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchV.clearFocus();
//                Utility.setHeadlinesProgressBargressBar(getWindow().getDecorView().findViewById(android.R.id.content),true);
               // processSearch(query);
                Intent intent = new Intent(that, SearchActivity.class);
                intent.putExtra("keyword", query);
                that.startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    public void callBingAutosuggest(String query) {
        queue = Volley.newRequestQueue(this);
        String url = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q=" + query;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        fillSuggestions(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Ocp-Apim-Subscription-Key", "e55248b17c204a3c8968bdee04023826");
                return params;
            }
        };
        queue.add(getRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    public void processSearch(String query) {
        Log.d("Volley", "bolley");
//        queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://10.0.2.2:8080/search?keyword=" + query;
        Utility util = new Utility();
        final Context that = this;
        final String keyword = query;
        util.getHomeFragmentData(url, this, new Utility.CallBack() {
            @Override
            public void dataLoaded(List<Card> cards) {
                Intent intent = new Intent(that, SearchActivity.class);
                intent.putExtra(EXTRA_MESSAGE, (Serializable) cards);
                intent.putExtra("keyword", keyword);
                that.startActivity(intent);
            }

            @Override
            public void dataError(String msg) {
                Log.d("ErrorMessage", msg);
            }
        });
    }

    public void fillSuggestions(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONArray("suggestionGroups").getJSONObject(0);
            JSONArray jsonArray = jsonObject.getJSONArray("searchSuggestions");
            List<String> suggestionsList = new ArrayList<String>();
            for (int i = 0; i < jsonArray.length(); i++) {
                suggestionsList.add(jsonArray.getJSONObject(i).getString("displayText"));
            }
            autoSuggestAdapter.setData(suggestionsList);
            autoSuggestAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}