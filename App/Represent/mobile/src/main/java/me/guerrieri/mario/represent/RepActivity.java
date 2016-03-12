package me.guerrieri.mario.represent;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

public class RepActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "mobile/RepActivity";
    private RepActivity activity = this;
    private ObjectAnimator oa;

    private UnloadedRepresentative rep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep);

        // the following courtesy of http://stackoverflow.com/a/26748694
        postponeEnterTransition();

        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });

        RecyclerView recyclerView = ((RecyclerView) this.findViewById(R.id.rep_info_view));
        recyclerView.setAdapter(this.repInfoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(TAG, "finishing");
//                activity.finish();
//            }
//        }, new IntentFilter(getString(R.string.close_rep_action)));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = this.getIntent();
//        String photoFileName = intent.getExtras().getBundle("rep").getString("photoFileName");
        this.rep = new UnloadedRepresentative(intent.getExtras().getBundle("rep"));
//        Bitmap photo = BitmapFactory.decodeFile(photoFileName);
//        ().setImageDrawable(
//                new BitmapDrawable(this.getResources(), photo)
//        );




//            holder.tile.setImageDrawable(
//                    new BitmapDrawable(activity.getResources(), rep.photo)
//            );

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
        }
    }

    @Override
    public void onClick(View v) {
        this.finishAfterTransition();
    }

    RecyclerView.Adapter repInfoAdapter = new RecyclerView.Adapter<RepInfoViewHolder>() {
        @Override
        public RepInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RepInfoViewHolder(LayoutInflater.from(parent.getContext())
                                                            .inflate(viewType, parent, false));
        }

        @Override
        public void onBindViewHolder(RepInfoViewHolder holder, int position) {
            position -= 1;
            if (position == -1) {
                View tile = holder.itemView.findViewById(R.id.rep_full_tile);
                tile.setTransitionName("tile");
                tile.findViewById(R.id.rep_expand).setVisibility(View.GONE);

                ((TextView) holder.itemView.findViewById(R.id.rep_name)).setText(rep.name);
                ((TextView) holder.itemView.findViewById(R.id.rep_desc)).setText(
                        String.format(getString(R.string.rep_desc_format), rep.type, rep.party, rep.state)
                );
                tile.setBackgroundColor(activity.getResources().getColor(rep.party.getColor()));

                if (rep.showBanner) {
                    Ion.with(activity)
                            .load(rep.bannerURL)
                            .withBitmap()
                            .placeholder(R.drawable.default_rep_image)
                            .error(R.drawable.default_rep_image)
                            .intoImageView((ImageView) holder.itemView.findViewById(R.id.rep_tile));
                }

                final int tweetStyle = rep.party == Representative.Party.dem ?
                        R.style.TweetDemStyle : rep.party == Representative.Party.rep ?
                        R.style.TweetRepStyle : R.style.TweetIndStyle;
                TweetUtils.loadTweet(rep.tweetID, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        FrameLayout frame = ((FrameLayout) activity.findViewById(R.id.rep_tweet_frame));
                        frame.addView(new TweetView(activity, result.data, tweetStyle));
                        frame.requestLayout();
                    }

                    @Override
                    public void failure(TwitterException e) {

                    }
                });

                holder.itemView.findViewById(R.id.rep_web).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(rep.website));
                        startActivity(i);
                    }
                });
            } else if (position == 0) {
                holder.title.setText(activity.getResources().getString(R.string.bills));
            } else if (position - 1 < activity.rep.bills.length) { // bill
                Bill bill = activity.rep.bills[position - 1];
                holder.title.setText(bill.title);
                holder.desc.setText(String.format(getString(R.string.bill_desc), bill.date));
                holder.divider.setVisibility(position - 1 == activity.rep.bills.length - 1 ? View.VISIBLE : View.GONE);
            } else if (position - 1 == activity.rep.bills.length) {
                holder.title.setText(activity.getResources().getString(R.string.committees));
            } else if (position - activity.rep.bills.length - 2 < activity.rep.committees.length) { // committee
                Committee committee = activity.rep.committees[position - activity.rep.bills.length - 2];
                holder.title.setText(committee.title);
                holder.desc.setText(String.format(getString(R.string.committee_desc), committee.desc));
            }
        }

        @Override
        public int getItemViewType(int position) {
            position -= 1;
            if (position == -1) return R.layout.rep_tile;
            if (position == 0) return R.layout.list_item_rep_header;
            else if (position - 1 < activity.rep.bills.length) return R.layout.list_item_rep_info;
            else if (position - 1 == activity.rep.bills.length) return R.layout.list_item_rep_header;
            else if (position - activity.rep.bills.length - 2 < activity.rep.committees.length) return R.layout.list_item_rep_info;
            else throw new RuntimeException("this probably shouldn't have happened");
        }

        @Override
        public int getItemCount() {
            return activity.rep.bills.length + activity.rep.committees.length + 3;
        }
    };

    class RepInfoViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView desc;
        private final View divider;

        public RepInfoViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.list_item_rep_title);
            this.desc = (TextView) itemView.findViewById(R.id.list_item_rep_date);
            this.divider = itemView.findViewById(R.id.list_item_rep_divider);
        }
    }
}
