package me.guerrieri.mario.represent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RepActivity extends AppCompatActivity {
    RepActivity activity = this;

    private String zip;
    private Representative rep;
    private Representative sen1;
    private Representative sen2;

    private Bill[] bills;
    private Committee[] committees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_rep);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);

        RecyclerView recyclerView = ((RecyclerView) this.findViewById(R.id.rep_info_view));
        recyclerView.setAdapter(this.repInfoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        View frame = this.findViewById(R.id.rep_info_frame);
        BottomSheetBehavior sheet = BottomSheetBehavior.from(frame);
        sheet.setPeekHeight(0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = this.getIntent();
        this.zip = intent.getExtras().getString(LocationActivity.EXTRA_ZIP);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle(String.format(getString(R.string.activity_rep_title_format), this.zip));

        this.rep = Representative.getRep(this.zip, this.getResources());
        this.bills = this.rep.bills;
        this.committees = this.rep.committees;
        ((ImageView) this.findViewById(R.id.rep_tile)).setImageDrawable(this.rep.photo);
        ((TextView) this.findViewById(R.id.rep_name)).setText(this.rep.name);
        ((TextView) this.findViewById(R.id.rep_desc)).setText(
                String.format(getString(R.string.rep_desc_format), rep.type, rep.party, rep.state)
        );
        this.findViewById(R.id.rep_tweet_box).setBackgroundColor(
                ResourcesCompat.getColor(this.getResources(), getPartyColorId(this.rep.party), null)
        );
        ((TextView) this.findViewById(R.id.rep_username)).setText(this.rep.username);
        ((TextView) this.findViewById(R.id.rep_tweet)).setText(this.rep.tweet);

        Representative[] sens = Representative.getSens(this.zip, this.getResources());
        this.sen1 = sens[0];
        this.sen2 = sens[1];


    }

    private int getPartyColorId(Representative.Party party) {
        if (party == Representative.Party.dem) return R.color.demColor;
        else if (party == Representative.Party.rep) return R.color.repColor;
        else return R.color.indColor;
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
            View frame = this.findViewById(R.id.rep_info_frame);
            BottomSheetBehavior sheet = BottomSheetBehavior.from(frame);
            sheet.setPeekHeight(
                    this.findViewById(R.id.content_rep).getHeight() -
                            this.findViewById(R.id.rep_tile).getHeight()
            );
            sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            frame.requestLayout();
            sheet.setHideable(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Button.OnClickListener repExpandListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    RecyclerView.Adapter repInfoAdapter = new RecyclerView.Adapter<RepInfoViewHolder>() {
        @Override
        public RepInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RepInfoViewHolder(LayoutInflater.from(parent.getContext())
                                                            .inflate(viewType, parent, false));
        }

        @Override
        public void onBindViewHolder(RepInfoViewHolder holder, int position) {
            if (0 < position && position - 1 < activity.bills.length) { // bill
                Bill bill = activity.bills[position - 1];
                holder.title.setText(bill.title);
                holder.desc.setText(String.format(getString(R.string.bill_desc), bill.date));
            } else if (position - 1 > activity.bills.length &&
                    position - activity.bills.length - 2 < activity.committees.length) { // committee
                Committee committee = activity.committees[position - activity.bills.length - 2];
                holder.title.setText(committee.title);
                holder.desc.setText(String.format(getString(R.string.committee_desc), committee.date));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return R.layout.list_item_rep_bills_header;
            else if (position - 1 < activity.bills.length) return R.layout.list_item_rep_info;
            else if (position - 1 == activity.bills.length) return R.layout.list_item_rep_committees_header;
            else if (position - activity.bills.length - 2 < activity.committees.length) return R.layout.list_item_rep_info;
            else throw new RuntimeException("everybody knows shit's fucked"); // TODO: don't submit this
        }

        @Override
        public int getItemCount() {
            return activity.bills.length + activity.committees.length + 2;
        }
    };

    class RepInfoViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView desc;

        public RepInfoViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.list_item_rep_title);
            this.desc = (TextView) itemView.findViewById(R.id.list_item_rep_date);
        }
    }
}
