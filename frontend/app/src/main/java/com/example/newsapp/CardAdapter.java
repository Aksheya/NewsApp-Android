package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

//import org.threeten.bp.Duration;
//import org.threeten.bp.LocalDateTime;
//import org.threeten.bp.ZoneId;
//import org.threeten.bp.ZonedDateTime;
//import org.threeten.bp.format.DateTimeFormatter;

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
    String url;
    String layoutName;
    String activityName = "";
    OnBookmarkClickListener onBookmarkClickListener;

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
        long seconds= TimeUnit.MILLISECONDS.toSeconds(current.getTime() - past.getTime());
        long minutes=TimeUnit.MILLISECONDS.toMinutes(current.getTime() - past.getTime());
        long hours=TimeUnit.MILLISECONDS.toHours(current.getTime() - past.getTime());
        long days=TimeUnit.MILLISECONDS.toDays(current.getTime() - past.getTime());
        String finalTime="";
        if(seconds<60)
        {
            finalTime = seconds+"s ago";
        }
        else if(minutes<60)
        {
          finalTime = minutes+"m ago";
        }
        else if(hours<24)
        {
           finalTime = hours+"h ago";
        }
        else
        {
           finalTime = days+"d ago";
        }

        String gridTime="";
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM");
        gridTime = outputFormat.format(past);




//        int index;
//        String timeAgo="";
//        try {
//            long time = sdf.parse(formattedString).getTime();
//            long now = System.currentTimeMillis();
////            long diff = (now - time)/1000;
////            if(diff<60){
////                timeAgo = diff + "s ago";
////            }
////            else if(diff< 3600)
////                timeAgo= diff/60 + "m ago";
////            else
////                timeAgo = diff/3600 + "h ago";
//            String ago = (String) DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS);
//            Log.d("ago",ago);
//            if(ago.contains("minute")){
//                index  = ago.indexOf("minute");
//                timeAgo = ago.substring(0,index-1) + "m ago";
//            }
//            else if(ago.contains("hour")){
//                index  = ago.indexOf("hour");
//                timeAgo = ago.substring(0,index-1) + "h ago";
//            }
//            else if(ago.contains("second")){
//                index  = ago.indexOf("second");
//                timeAgo = ago.substring(0,index-1) + "s ago";
//            }
//            else{
//                timeAgo = ago;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        ZonedDateTime zdtnow = ZonedDateTime.now();
//        simpledatetime
//        DateUtils.getRelativeDateTimeString()



//        Duration zonedDateTime = Duration.between(zdtnow, zdtold);
//        long seconds = zonedDateTime.getSeconds();
//        String hms = "";
//        if (seconds < 60)
//            hms = seconds + "s ago";
//        else if (seconds < 3600)
//            hms = (seconds / 60) + "m ago";
//        else
//            hms = (seconds / 3600) + "h ago";


        holder.cardTitle.setText(cardList.get(position).title);
        holder.cardSection.setText(cardList.get(position).section);
        if(layoutName.equals("gridLayout"))
           holder.cardTime.setText(gridTime);
        else
            holder.cardTime.setText(finalTime);
        url = cardList.get(position).image;
        Picasso.with(context).load(url).resize(100, 100).into(holder.cardImage);
        holder.cardImage.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                return;
            }
        } );
        setImage(holder.cardBookmark, position);
    }


    public void setImage(ImageView imageBookmark, int position) {
        SharedPreferences pref = context.getSharedPreferences("BookmarkPref", 0); // 0 - for private mode
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

    public class CardViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        TextView cardTitle;
        TextView cardTime;
        TextView cardSection;
        ImageView cardImage;
        ImageView cardBookmark;
        public static final String EXTRA_MESSAGE = "DetailedArticleId";

        public CardViewHolder(@NonNull View view) {
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
                    Picasso.with(context).load(url).into(image);
                    setImage((ImageView) dialog.findViewById(R.id.dialogBookmark), position);
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
                            if(layoutName.equals("gridLayout")){
                                onBookmarkClickListener.onBookmarkClick(getAdapterPosition());
                                dialog.hide();
                            }
                        }
                    });
                    ImageView dialogTwitter = (ImageView)dialog.findViewById(R.id.dialogTwitter);
                    dialogTwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = "https://twitter.com/intent/tweet?text=Check out this Link:&url=" + cardList.get(getAdapterPosition()).shareUrl + "&hashtags=CSCI571NewsSearch";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            context.startActivity(i);
                        }
                    });
//                    Uri path = Uri.parse("android.resource://com.example.newsapp/" + R.drawable.baseline_bookmark_border_black_18dp);
//                    bookmarkImage.setImageURI(path);
//                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
//                    bookmarkImage.setLayoutParams(layoutParams);
//                    Uri twitterPath = Uri.parse("android.resource://com.example.newsapp/" + R.drawable.bluetwitter);
//                    ImageView twitterImage = (ImageView)dialog.findViewById(R.id.dialogTwitter);
//                    twitterImage.setImageURI(twitterPath);
//                    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(100, 100);
//                    twitterImage.setLayoutParams(layoutParams1);

//
//                    Button declineButton = (Button) dialog.findViewById(R.id.declineButton);
//                    // if decline button is clicked, close the custom dialog
//                    declineButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            // Close dialog
//                            dialog.dismiss();
//                        }
//                    });

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
                    if(layoutName.equals("gridLayout")){
                        onBookmarkClickListener.onBookmarkClick(getAdapterPosition());
                    }
                }
            });
        }


//        @Override
//        public String toString() {
//            return super.toString() + " '" + cardTitle.getText() + "'";
//        }


    }
}
