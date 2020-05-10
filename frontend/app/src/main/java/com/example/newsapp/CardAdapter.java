package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private Context context;
    private final List<Card> cardList;
    private String url;
    private String layoutName;
    private String activityName = "";
    private OnBookmarkClickListener onBookmarkClickListener;


    public CardAdapter(List<Card> cards, String activityName, String layoutName, OnBookmarkClickListener onBookmarkClickListener) {
        cardList = cards;
        this.activityName = activityName;
        this.layoutName = layoutName;
        this.onBookmarkClickListener = onBookmarkClickListener;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (layoutName.equals("gridLayout"))
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bookmark, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        context = parent.getContext();
//        shareDialog = new ShareDialog(parent.);
        return new CardViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        String timeGiven = cardList.get(position).time;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime dateTime = LocalDateTime.parse(timeGiven, formatter);
        ZoneId zoneId = ZoneId.of("America/Los_Angeles");
        ZonedDateTime zdtold = dateTime.atZone(zoneId);
        String formattedString = zdtold.format(formatter);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date past = null;
        try {
            past = sdf.parse(formattedString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date current = new Date();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(current.getTime() - past.getTime());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(current.getTime() - past.getTime());
        long hours = TimeUnit.MILLISECONDS.toHours(current.getTime() - past.getTime());
        long days = TimeUnit.MILLISECONDS.toDays(current.getTime() - past.getTime());
        String finalTime = "";
        if (seconds < 60) {
            finalTime = seconds + "s ago";
        } else if (minutes < 60) {
            finalTime = minutes + "m ago";
        } else if (hours < 24) {
            finalTime = hours + "h ago";
        } else {
            finalTime = days + "d ago";
        }
        String gridTime = "";
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM");
        gridTime = outputFormat.format(past);
        holder.cardTitle.setText(cardList.get(position).title);
        holder.cardSection.setText(cardList.get(position).section);
        if (layoutName.equals("gridLayout"))
            holder.cardTime.setText(gridTime);
        else
            holder.cardTime.setText(finalTime);
        url = cardList.get(position).image;
        Picasso.with(context).load(url).resize(100, 100).into(holder.cardImage);
        holder.cardImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        setImage(holder.cardBookmark, position);
    }


    private void setImage(ImageView imageBookmark, int position) {
        SharedPreferences pref = context.getSharedPreferences("BookmarkPref", 0);
        Card myCardObject = cardList.get(position);
        String objectstring = pref.getString(myCardObject.articleId, null);
        String uri;
        if (objectstring != null) {
            uri = "@drawable/ic_bookmark_black_24dp";
        } else {
            uri = "@drawable/ic_bookmark_border_black_24dp";
        }
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        Drawable res = context.getResources().getDrawable(imageResource);
        imageBookmark.setImageDrawable(res);
    }


    @Override
    public int getItemCount() {
        return cardList.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        TextView cardTitle;
        TextView cardTime;
        TextView cardSection;
        ImageView cardImage;
        ImageView cardBookmark;
        static final String EXTRA_MESSAGE = "DetailedArticleId";

        CardViewHolder(@NonNull View view) {
            super(view);
            cardView = view;
            cardTitle = cardView.findViewById(R.id.card_title);
            cardTime = cardView.findViewById(R.id.cardTime);
            cardSection = cardView.findViewById(R.id.cardSection);
            cardImage = (ImageView) cardView.findViewById(R.id.cardImage);
            cardBookmark = (ImageView) cardView.findViewById(R.id.cardBookmark);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(context, DetailActivity.class);
                    String message = cardList.get(position).articleId;
                    intent.putExtra(EXTRA_MESSAGE, message);
                    intent.putExtra("goTo", activityName);
                    context.startActivity(intent);
                }
            });
            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_box);
                    dialog.setTitle("Custom Dialog");
                    TextView text = (TextView) dialog.findViewById(R.id.dialogText);
                    text.setText(cardTitle.getText());
                    ImageView image = (ImageView) dialog.findViewById(R.id.dialogImage);
                    url = cardList.get(position).image;
                    Picasso.with(context).load(url).into(image);
                    Window window = dialog.getWindow();
                    int width = (int)(context.getResources().getDisplayMetrics().widthPixels*0.90);
                    window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                    setImage((ImageView) dialog.findViewById(R.id.dialogBookmark), position);
                    dialog.getWindow().setBackgroundDrawable(new
                            ColorDrawable(Color.TRANSPARENT));

                    dialog.show();
                    final ImageView dialogBookmarkImage = (ImageView) dialog.findViewById(R.id.dialogBookmark);
                    dialogBookmarkImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String uri = Utility.handleBookmark(cardList.get(getAdapterPosition()), context);
                            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
                            Drawable res = context.getResources().getDrawable(imageResource);
                            dialogBookmarkImage.setImageDrawable(res);
                            cardBookmark.setImageDrawable(res);
                            if (layoutName.equals("gridLayout")) {
                                onBookmarkClickListener.onBookmarkClick(getAdapterPosition());
                                dialog.hide();
                            }
                        }
                    });
                    ImageView dialogTwitter = (ImageView) dialog.findViewById(R.id.dialogTwitter);
                    dialogTwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urli= "https://twitter.com/intent/tweet?text=Check out this Link:&url=" + cardList.get(getAdapterPosition()).shareUrl + "&hashtags=CSCI571NewsSearch";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(urli));
                            context.startActivity(i);
                        }
                    });

                    ImageView dialogFb = (ImageView) dialog.findViewById(R.id.dialogFb);
                    dialogFb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ShareLinkContent content = new ShareLinkContent.Builder()
                                    .setQuote("Check out this Link:")
                                    .setContentUrl(Uri.parse(cardList.get(getAdapterPosition()).shareUrl))
                                    .setShareHashtag(new ShareHashtag.Builder()
                                            .setHashtag("#CSCI571")
                                            .build())
                                    .build();
//                            Class classn;
//                            try {
//                                classn = Class.forName("com.example.newsapp." + activityName);
//                            } catch (ClassNotFoundException e) {
//                                e.printStackTrace();
//                            }
                            if (ShareDialog.canShow(ShareLinkContent.class)) {
                                    Activity activity = (Activity)context;
                                    ShareDialog.show(activity, content);
                            }
                        }
                    });
                    return true;
                }
            });
            cardBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = Utility.handleBookmark(cardList.get(getAdapterPosition()), context);
                    int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
                    Drawable res = context.getResources().getDrawable(imageResource);
                    cardBookmark.setImageDrawable(res);
                    if (layoutName.equals("gridLayout")) {
                        onBookmarkClickListener.onBookmarkClick(getAdapterPosition());
                    }
                }
            });
        }
    }
}
