package me.guerrieri.mario.represent;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class RepActivity extends Activity implements View.OnClickListener {
    private RepActivity activity = this;
    private ObjectAnimator oa;

    private Representative rep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = this.getIntent();
        this.rep = new Representative(intent.getExtras().getBundle("rep"));

        CoordinatorLayout coordinatorLayout = ((CoordinatorLayout) this.findViewById(R.id.content_rep));
        View tile = LayoutInflater.from(this).inflate(
                this.rep.type == Representative.RepType.rep ?
                        R.layout.rep_tile :
                        R.layout.sen_tile,
                coordinatorLayout, false);
        tile.setTransitionName("tile");
        coordinatorLayout.addView(tile);

        Resources resources = this.getResources();

        ((ImageView) this.findViewById(R.id.rep_tile)).setImageDrawable(
                ResourcesCompat.getDrawable(resources, rep.photoId, null)
        );
        ((TextView) this.findViewById(R.id.rep_name)).setText(rep.name);
        ((TextView) this.findViewById(R.id.rep_desc)).setText(
                String.format(getString(R.string.rep_desc_format), rep.type, rep.party, rep.state)
        );
        this.findViewById(R.id.rep_tweet_box).setBackgroundColor(
                ResourcesCompat.getColor(resources, this.rep.party.getColor(), null)
        );
        ((TextView) this.findViewById(R.id.rep_username)).setText(rep.username);
        ((TextView) this.findViewById(R.id.rep_tweet)).setText(rep.tweet);

        View expandButton = this.findViewById(R.id.rep_expand);
        expandButton.setOnClickListener(this);
        expandButton.setRotation(180);

        RecyclerView recyclerView = ((RecyclerView) this.findViewById(R.id.rep_info_view));
        recyclerView.setAdapter(this.repInfoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            View frame = this.findViewById(R.id.rep_info_frame);
            BottomSheetBehavior sheet = BottomSheetBehavior.from(frame);
            int coordinatorLayoutHeight = this.findViewById(R.id.content_rep).getHeight();
            int tileHeight = this.findViewById(R.id.rep_tile).getHeight();
            int peekHeight = coordinatorLayoutHeight - tileHeight;
            sheet.setPeekHeight(peekHeight);
        }
    }

    @Override
    public void onClick(View v) {
        int coordinatorLayoutHeight = this.findViewById(R.id.content_rep).getHeight();
        int tileHeight = this.findViewById(R.id.rep_tile).getHeight();
        int peekHeight = coordinatorLayoutHeight - tileHeight;
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
            if (position == 0) {
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
                holder.desc.setText(String.format(getString(R.string.committee_desc), committee.date));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return R.layout.list_item_rep_header;
            else if (position - 1 < activity.rep.bills.length) return R.layout.list_item_rep_info;
            else if (position - 1 == activity.rep.bills.length) return R.layout.list_item_rep_header;
            else if (position - activity.rep.bills.length - 2 < activity.rep.committees.length) return R.layout.list_item_rep_info;
            else throw new RuntimeException("this probably shouldn't have happened");
        }

        @Override
        public int getItemCount() {
            return activity.rep.bills.length + activity.rep.committees.length + 2;
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
