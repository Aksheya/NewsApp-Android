package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public class DetailActivity extends AppCompatActivity {
    private RequestQueue queue;
    private ActionBar ab;
    private String shareUrl;
    private Card card;
    private String id;
    CallbackManager callbackManager;
    ShareDialog shareDialog;

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
        Utility.setProgressBar(getWindow().getDecorView().findViewById(android.R.id.content), true);
        findViewById(R.id.detail_article_card).setVisibility(View.GONE);
        queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://10.0.2.2:8080/detail_article?articleId=" + extras.getString("DetailedArticleId");
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
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

    }


    @Override
    public Intent getSupportParentActivityIntent() {
        return goToParent();
    }

    @Override
    public Intent getParentActivityIntent() {
        return goToParent();
    }

    private Intent goToParent() {
        Intent intent = null;
        Bundle extras = getIntent().getExtras();
        String goTo = extras.getString("goTo");
        if (goTo.equals("SearchActivity")) {
            intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu_items, menu);
        final RelativeLayout rel = findViewById(R.id.rel);
        MenuItem item = menu.findItem(R.id.detailCardBookmark);

        setImage(id, item);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("card", card.title);
                String uri = Utility.handleBookmark(card, getApplicationContext());
                int imageResource = getApplicationContext().getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
                item.setIcon(ContextCompat.getDrawable(getApplicationContext(), imageResource));
                return false;
            }
        });
//        item = menu.findItem(R.id.detailTwitter);
//        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                String url = "https://twitter.com/intent/tweet?text=Check out this Link:&url=" + shareUrl + "&hashtags=CSCI571NewsSearch";
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//                return false;
//            }
//        });

        item = menu.findItem(R.id.detailShare);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("Blah", "blah");
                final Dialog dialog = new Dialog(DetailActivity.this);
                Window window = dialog.getWindow();
                window.setGravity(Gravity.BOTTOM);
                dialog.setContentView(R.layout.share_dialog);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setTitle("Custom Dialog");
                dialog.show();
                ImageButton button = (ImageButton) dialog.findViewById(R.id.detailShareTwitter);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "https://twitter.com/intent/tweet?text=Check out this Link:&url=" + shareUrl + "&hashtags=CSCI571NewsSearch";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                ImageButton fbButton = (ImageButton) dialog.findViewById(R.id.detailShareFacebook);
                fbButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShareLinkContent content = new ShareLinkContent.Builder()
                                .setQuote("Check out this Link:")
                                .setContentUrl(Uri.parse(shareUrl))
                                .setShareHashtag(new ShareHashtag.Builder()
                                        .setHashtag("#CSCI571")
                                        .build())
                                .build();
                        if (ShareDialog.canShow(ShareLinkContent.class)) {
                            shareDialog.show(content);
                        }
                    }
                });
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
            case R.id.detailShare:
                return false;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void processResponse(JSONObject response) {
        try {
            String time = response.getString("time");
            String section = response.getString("section");
            String title = response.getString("title");
            shareUrl = response.getString("shareUrl");
            String description = response.getString("description");
            String imageUrl = response.getString("image");
            String articleId = response.getString("articleId");
            card = new Card(section, time, title, articleId, shareUrl, imageUrl);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
            ZoneId zoneId = ZoneId.of("America/Los_Angeles");
            ZonedDateTime zdtold = dateTime.atZone(zoneId);
            String dateFormatted = DateTimeFormatter.ofPattern("dd MMM yyyy").format(zdtold);
            ((TextView) findViewById(R.id.detailedArticleTime)).setText(dateFormatted);
            ((TextView) findViewById(R.id.detailedArticleSection)).setText(section);
            ((TextView) findViewById(R.id.detailedArticleTitle)).setText(title);
            ab.setTitle(title);
            TextView desct = (TextView) findViewById(R.id.detailedArticleDescription);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                desct.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
            } else
                desct.setText(Html.fromHtml(description));
            ((TextView) findViewById(R.id.detailedArticleShowMore)).setText(Html.fromHtml("<a href=\"" + shareUrl + "\">View Full Article</a>"));
            ((TextView) findViewById(R.id.detailedArticleShowMore)).setMovementMethod(LinkMovementMethod.getInstance());
            ImageView detailedArticleImageView = (ImageView) findViewById(R.id.detailedArticleImage);
            Picasso.with(getApplicationContext()).load(imageUrl).into(detailedArticleImageView);
            Utility.setProgressBar(getWindow().getDecorView().findViewById(android.R.id.content), false);
            findViewById(R.id.detail_article_card).setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setImage(String articleId, MenuItem mitem) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("BookmarkPref", 0); // 0 - for private mode
        String objectstring = pref.getString(articleId, null);
        String uri;
        if (objectstring != null) {
            uri = "@drawable/ic_bookmark_black_24dp";
        } else {
            uri = "@drawable/ic_bookmark_border_black_24dp";
        }
        int imageResource = getApplicationContext().getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
        mitem.setIcon(ContextCompat.getDrawable(getApplicationContext(), imageResource));
    }


}
