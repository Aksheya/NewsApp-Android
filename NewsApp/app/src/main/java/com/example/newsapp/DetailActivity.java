package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class DetailActivity extends AppCompatActivity {
    private RequestQueue queue;
    private ActionBar ab;
    private String shareUrl;
    private Card card;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        if (findViewById(R.id.fragment_bottom_container) != null) {
            if (savedInstanceState != null)
                return;
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myMenuBar);
        setSupportActionBar(myToolbar);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        id = extras.getString("DetailedArticleId");
        Utility.setHeadlinesProgressBar(getWindow().getDecorView().findViewById(android.R.id.content),true);
        findViewById(R.id.detail_article_card).setVisibility(View.GONE);
        queue = Volley.newRequestQueue(getApplicationContext());
        String url="http://10.0.2.2:8080/detail_article?articleId=" +  extras.getString("DetailedArticleId");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            processResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("MYERROR", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }


    @Override
    public Intent getSupportParentActivityIntent(){
         return goToParent();
    }

    @Override
    public Intent getParentActivityIntent(){
        return goToParent();
    }

    private Intent goToParent(){
        Intent intent = null;
        Bundle extras = getIntent().getExtras();
        String goTo = extras.getString("goTo");
        if(goTo.equals("SearchActivity")){
            intent  = new Intent(this,SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return intent;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu_items, menu);
        MenuItem item = menu.findItem(R.id.detailCardBookmark);
        setImage(id,item);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("card",card.title);
                String uri = Utility.handleBookmark(card,getApplicationContext());
                if(uri.equals("@drawable/ic_bookmark_border_black_24dp"))
                    uri = "@drawable/detail_bookmark_border";
                else
                    uri = "@drawable/detail_bookmark";
                int imageResource = getApplicationContext().getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
//                    Drawable res = getApplicationContext().getResources().getDrawable(imageResource);
                item.setIcon(ContextCompat.getDrawable(getApplicationContext(), imageResource));

                return false;
            }
        });
        item = menu.findItem(R.id.detailTwitter);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String url = "https://twitter.com/intent/tweet?text=Check out this Link:&url=" + shareUrl + "&hashtags=CSCI571NewsSearch";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MenuItem mitem = item;
        switch (item.getItemId()) {
            case R.id.detailCardBookmark:
                return false;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void processResponse(JSONObject response){
        try {
            String time = response.getString("time");
            String section = response.getString("section");
            String title = response.getString("title");
            shareUrl = response.getString("shareUrl");
            String description = response.getString("description");
            String imageUrl = response.getString("image");
            String articleId = response.getString("articleId");
            card = new Card(section,time,title,articleId,shareUrl,imageUrl);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
            ZoneId zoneId = ZoneId.of("America/Los_Angeles");
            ZonedDateTime zdtold = dateTime.atZone(zoneId);
            String dateFormatted = DateTimeFormatter.ofPattern("dd MMM yyyy").format(zdtold);
            ((TextView)findViewById(R.id.detailedArticleTime)).setText(dateFormatted);
            ((TextView)findViewById(R.id.detailedArticleSection)).setText(section);
            ((TextView)findViewById(R.id.detailedArticleTitle)).setText(title);
            ab.setTitle(title);
            TextView desct = (TextView)findViewById(R.id.detailedArticleDescription);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                desct.setText(Html.fromHtml(description,Html.FROM_HTML_MODE_LEGACY));
            }
            else
                desct.setText(Html.fromHtml(description));
            ((TextView)findViewById(R.id.detailedArticleShowMore)).setText(Html.fromHtml("<a href=\"" + shareUrl + "\">View Full Article</a>"));
            ((TextView)findViewById(R.id.detailedArticleShowMore)).setMovementMethod(LinkMovementMethod.getInstance());
            ImageView detailedArticleImageView = (ImageView)findViewById(R.id.detailedArticleImage);
            Picasso.with(getApplicationContext()).load(imageUrl).into(detailedArticleImageView);
            Utility.setHeadlinesProgressBar(getWindow().getDecorView().findViewById(android.R.id.content),false);
            findViewById(R.id.detail_article_card).setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public  void setImage(String articleId, MenuItem mitem)
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("BookmarkPref", 0); // 0 - for private mode
        String objectstring = pref.getString(articleId, null);
        String uri;
        if(objectstring != null){
            uri="@drawable/detail_bookmark";
        }
        else {
            uri = "@drawable/detail_bookmark_border";
        }
        int imageResource = getApplicationContext().getResources().getIdentifier(uri, null,getApplicationContext().getPackageName());
        mitem.setIcon(ContextCompat.getDrawable(getApplicationContext(), imageResource));
    }
}
