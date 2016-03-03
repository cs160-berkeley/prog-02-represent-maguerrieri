package me.guerrieri.mario.represent;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class RepListActivity extends AppCompatActivity {
    RepListActivity activity = this;

    private String zip;
    private Representative[] reps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_rep_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);

        RecyclerView recyclerView = ((RecyclerView) this.findViewById(R.id.rep_list));
        recyclerView.setAdapter(this.repListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) this.zip = extras.getString(LocationActivity.EXTRA_ZIP);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle(String.format(getString(R.string.activity_rep_title_format), this.zip));

        this.reps = this.getReps(this.zip);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.menu_rep, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            View frame = this.findViewById(R.id.rep_info_frame);
//            BottomSheetBehavior sheet = BottomSheetBehavior.from(frame);
//            sheet.setPeekHeight(
//                    this.findViewById(R.id.content_rep).getHeight() -
//                            this.findViewById(R.id.rep_tile).getHeight()
//            );
//            sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            frame.requestLayout();
//            sheet.setHideable(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Representative[] getReps(String zip) {
        // TODO: replace with API calls
        Resources resources = this.getResources();

        // Representative
        String[] billNames = resources.getStringArray(R.array.default_rep_bill_names);
        String[] billDates = resources.getStringArray(R.array.default_rep_bill_dates);
        Bill[] bills = new Bill[billNames.length];
        for (int i = 0; i < billNames.length; i++) {
            bills[i] = new Bill(billNames[i], billDates[i]);
        }
        String[] committeeNames = resources.getStringArray(R.array.default_rep_committee_names);
        String[] committeeDates = resources.getStringArray(R.array.default_rep_committee_dates);
        Committee[] committees = new Committee[committeeNames.length];
        for (int i = 0; i < committeeNames.length; i++) {
            committees[i] = new Committee(committeeNames[i], committeeDates[i]);
        }

        // Senator 1
        String[] billNames1 = resources.getStringArray(R.array.default_sen1_bill_names);
        String[] billDates1 = resources.getStringArray(R.array.default_sen1_bill_dates);
        Bill[] bills1 = new Bill[billNames1.length];
        for (int i = 0; i < billNames1.length; i++) {
            bills1[i] = new Bill(billNames1[i], billDates1[i]);
        }
        String[] committeeNames1 = resources.getStringArray(R.array.default_sen1_committee_names);
        String[] committeeDates1 = resources.getStringArray(R.array.default_sen1_committee_dates);
        Committee[] committees1 = new Committee[committeeNames1.length];
        for (int i = 0; i < committeeNames1.length; i++) {
            committees1[i] = new Committee(committeeNames1[i], committeeDates1[i]);
        }

        // Senator 2
        String[] billNames2 = resources.getStringArray(R.array.default_sen2_bill_names);
        String[] billDates2 = resources.getStringArray(R.array.default_sen2_bill_dates);
        Bill[] bills2 = new Bill[billNames2.length];
        for (int i = 0; i < billNames2.length; i++) {
            bills2[i] = new Bill(billNames2[i], billDates2[i]);
        }
        String[] committeeNames2 = resources.getStringArray(R.array.default_sen2_committee_names);
        String[] committeeDates2 = resources.getStringArray(R.array.default_sen2_committee_dates);
        Committee[] committees2 = new Committee[committeeNames2.length];
        for (int i = 0; i < committeeNames2.length; i++) {
            committees2[i] = new Committee(committeeNames2[i], committeeDates2[i]);
        }

        return new Representative[]{
                new Representative(
                        resources.getString(R.string.default_rep_name),
                        Representative.RepType.rep,
                        resources.getString(R.string.default_rep_state),
                        Representative.Party.values()[resources.getInteger(R.integer.default_rep_party)],
                        R.drawable.default_rep_image,
                        resources.getString(R.string.default_rep_username),
                        resources.getString(R.string.default_rep_tweet),
                        bills, committees
                ),
                new Representative(
                        resources.getString(R.string.default_sen1_name),
                        Representative.RepType.sen,
                        resources.getString(R.string.default_sen1_state),
                        Representative.Party.values()[resources.getInteger(R.integer.default_sen1_party)],
                        R.drawable.default_sen1_image,
                        resources.getString(R.string.default_sen1_username),
                        resources.getString(R.string.default_sen1_tweet),
                        bills1, committees1
                ),
                new Representative(
                        resources.getString(R.string.default_sen2_name),
                        Representative.RepType.sen,
                        resources.getString(R.string.default_sen2_state),
                        Representative.Party.values()[resources.getInteger(R.integer.default_sen2_party)],
                        R.drawable.default_sen2_image,
                        resources.getString(R.string.default_sen2_username),
                        resources.getString(R.string.default_sen2_tweet),
                        bills2, committees2
                )
        };
    }

    RecyclerView.Adapter repListAdapter = new RecyclerView.Adapter<RepListViewHolder>() {
        @Override
        public RepListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RepListViewHolder(LayoutInflater.from(parent.getContext())
                                                    .inflate(viewType, parent, false));
        }

        @Override
        public void onBindViewHolder(RepListViewHolder holder, int position) {
            Resources resources = activity.getResources();
            Representative rep = activity.reps[position];

            holder.item = rep;

            holder.tile.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, rep.photoId, null)
            );
            holder.name.setText(rep.name);
            holder.desc.setText(
                    String.format(getString(R.string.rep_desc_format), rep.type, rep.party, rep.state)
            );
            holder.tweetBox.setBackgroundColor(
                    ResourcesCompat.getColor(resources, rep.party.getColor(), null)
            );
            holder.username.setText(rep.username);
            holder.tweet.setText(rep.tweet);
        }

        @Override
        public int getItemCount() {
            return activity.reps.length;
        }

        @Override
        public int getItemViewType(int position) {
            return activity.reps[position].type == Representative.RepType.rep ?
                    R.layout.rep_tile :
                    R.layout.sen_tile;
        }
    };

    class RepListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Representative item;

        public final View itemView;

        public final ImageView tile;
        public final TextView name;
        public final TextView desc;
        public final View tweetBox;
        public final TextView username;
        public final TextView tweet;
        public final ImageButton expandButton;

        public RepListViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            this.tile = (ImageView) itemView.findViewById(R.id.rep_tile);
            this.name = (TextView) itemView.findViewById(R.id.rep_name);
            this.desc = (TextView) itemView.findViewById(R.id.rep_desc);
            this.tweetBox = itemView.findViewById(R.id.rep_tweet_box);
            this.username = (TextView) itemView.findViewById(R.id.rep_username);
            this.tweet = (TextView) itemView.findViewById(R.id.rep_tweet);
            this.expandButton = (ImageButton) itemView.findViewById(R.id.rep_expand);

            this.expandButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.itemView.setTransitionName("tile");
            activity.startActivity(
                    new Intent(activity, RepActivity.class).putExtra("rep", this.item.toBundle()),
                    ActivityOptionsCompat
                            .makeSceneTransitionAnimation(activity, this.itemView, "tile").toBundle()
            );
        }
    }
}
