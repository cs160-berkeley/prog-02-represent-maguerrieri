package me.guerrieri.mario.represent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

public class RepListActivity extends AppCompatActivity {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "try again later";
    private static final String TWITTER_SECRET = "SAFE";

    private static final String SUNLIGHT_KEY = "so secure";

    private static final String TAG = "RepListActivity";
    RepListActivity activity = this;

    private GoogleApiClient googleApiClient;
    private TwitterApiClient twitterApiClient;
    private String twitterToken;

    private String zip;
    private double lat;
    private double lng;
    private String county;

    private ArrayList<UnloadedRepresentative> reps;
    private HashMap<Integer, RepListViewHolder> viewHolders;
    private JsonObject electionData;
    private boolean gotLatLong;
    private int repsLoaded;
    private int totalReps;
    private JsonArray locList;

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

        this.viewHolders = new HashMap<>();

        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addApi(LocationServices.API)
                .build();
        this.googleApiClient.connect();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> result) {
                activity.twitterApiClient = TwitterCore.getInstance().getApiClient(result.data);
                Log.d(TAG, "Twitter auth succeeded.");
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "Twitter auth failed.");
            }
        });

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, String.format("got rep change %s", intent.getExtras().getString("rep")));
                String name = intent.getExtras().getString("rep");
                for (UnloadedRepresentative rep : activity.reps) {
                    if (rep.name.equals(name)) {
                        activity.switchToRep(rep, null);
                        break;
                    }
                }
            }
        }, new IntentFilter(this.getString(R.string.rep_changed_action)));

        lbm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "got random loc");
                activity.randomLoc();
            }
        }, new IntentFilter(this.getString(R.string.rep_random_action)));

        this.electionData = this.loadJsonFromResource(R.raw.election).getAsJsonObject();
        this.locList = this.loadJsonFromResource(R.raw.zips).getAsJsonArray();

        this.reps = new ArrayList<>();
    }

    private JsonElement loadJsonFromResource(int id) {
        // http://stackoverflow.com/questions/6349759/using-json-file-in-android-app-resources
        InputStream in = this.getResources().openRawResource(id);
        StringWriter writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JsonParser().parse(writer.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = this.getIntent().getExtras();
        this.gotLatLong = false;
        if (extras != null) {
            String oldZip = this.zip;
            if (extras.containsKey(this.getString(R.string.zip_bundle_key))) {
                this.zip = extras.getString(this.getString(R.string.zip_bundle_key));
                if (!this.zip.equals(oldZip)) { // if we got a new zip
                    Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
                    toolbar.setTitle(String.format(getString(R.string.activity_rep_title_format), this.zip));
                    this.getReps(this.zip, 0, 0);
                }
            } else this.zip = null;
            if (extras.containsKey(this.getString(R.string.lat_bundle_key))) {
                this.gotLatLong = true;
                double oldLat = this.lat;
                double oldLong = this.lng;
                this.lat = extras.getDouble(this.getString(R.string.lat_bundle_key));
                this.lng = extras.getDouble(this.getString(R.string.long_bundle_key));
                if ((this.lat != oldLat || this.lng != oldLong || oldZip != null) && this.zip == null) {
                    Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
                    toolbar.setTitle(String.format(getString(R.string.activity_rep_title_format),
                            String.format("%.2f, %.2f", this.lat, this.lng)));
                    this.getReps(null, this.lat, this.lng);
                }
            }

            FutureCallback<JsonObject> latLongCallback = new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (!activity.gotLatLong) {
                        JsonObject coords = result.get("results").getAsJsonArray()
                                .get(0).getAsJsonObject()
                                .get("geometry").getAsJsonObject()
                                .get("location").getAsJsonObject();
                        activity.lat = coords.get("lat").getAsDouble();
                        activity.lng = coords.get("lng").getAsDouble();
                    }

                    Ion.with(activity)
                            .load(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f", activity.lat, activity.lng))
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    JsonArray results = result.get("results").getAsJsonArray();
                                    String county = null;
                                    String state = null;
                                    for (JsonElement obj : results) {
                                        county = null;
                                        state = null;
                                        JsonArray components = obj.getAsJsonObject()
                                                .get("address_components").getAsJsonArray();
                                        for (JsonElement elm : components) {
                                            JsonArray types = elm.getAsJsonObject()
                                                    .get("types").getAsJsonArray();
                                            if (types.get(0).getAsString()
                                                    .equalsIgnoreCase("administrative_area_level_2")) {
                                                county = elm.getAsJsonObject()
                                                        .get("long_name").getAsString();
                                            } else if (types.get(0).getAsString()
                                                    .equalsIgnoreCase("administrative_area_level_1")) {
                                                state = elm.getAsJsonObject()
                                                        .get("short_name").getAsString();
                                            }
                                        }
                                        if (county != null && state != null) break;
                                    }
                                    if (county != null && state != null) {
                                        activity.county = String.format("%s, %s", county, state);
                                    }
                                    Intent updateWatchIntent = new Intent(activity.getBaseContext(), PhoneToWatchService.class);
                                    updateWatchIntent.putExtra("type", "loc");
                                    updateWatchIntent.putExtra("county", activity.county);
                                    JsonObject election = activity.electionData
                                            .get(activity.county).getAsJsonObject();
                                    updateWatchIntent.putExtra("obama", election
                                            .get("obama").getAsDouble());
                                    updateWatchIntent.putExtra("romney", election
                                            .get("romney").getAsDouble());
                                    activity.startService(updateWatchIntent);
                                }
                            });
                }
            };

            if (!this.gotLatLong && this.zip != null) {
                Ion.with(this)
                        .load(String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s", this.zip))
                        .asJsonObject()
                        .setCallback(latLongCallback);
            } else {
                latLongCallback.onCompleted(null, null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.menu_rep, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.googleApiClient.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.random) {
            this.randomLoc();
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

    public void addRep(UnloadedRepresentative rep) {
        this.reps.add(rep);
        this.repListAdapter.notifyItemInserted(this.reps.size() - 1);
        this.repsLoaded ++;

        if (this.repsLoaded >= this.totalReps) {
            Intent updateWatchIntent = new Intent(this.getBaseContext(), PhoneToWatchService.class);
            updateWatchIntent.putExtra("type", "reps");
            for (int i = 0; i < this.reps.size(); i ++)
                updateWatchIntent.putExtra(Integer.toString(i), this.reps.get(i).toBundle());
            this.startService(updateWatchIntent);
        }
    }

    public void getReps(final String zip, final double lat, final double lng) {
        final ArrayList<Bitmap> photos = new ArrayList<>();
        Ion.with(this) // get Twitter auth
                .load("https://api.twitter.com/oauth2/token")
                .basicAuthentication(TWITTER_KEY, TWITTER_SECRET)
                .setBodyParameter("grant_type", "client_credentials")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        activity.twitterToken = result.get("access_token").getAsString();

                        Ion.with(activity) // get representatives
                                .load(String.format("http://congress.api.sunlightfoundation.com/legislators/locate?%s&apikey=%s",
                                        (zip != null ?
                                                String.format("zip=%s", zip) :
                                                String.format("latitude=%f&longitude=%f", lat, lng)
                                        ), SUNLIGHT_KEY))
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject repsResult) {
                                        Log.d(TAG, repsResult.toString());
                                        JsonArray repObjs = repsResult.getAsJsonArray("results");
                                        activity.repsLoaded = 0;
                                        activity.totalReps = repObjs.size();
                                        for (final JsonElement item : repObjs) {
                                            final JsonObject repObj = item.getAsJsonObject();
                                            final String bioID = repObj.get("bioguide_id").getAsString();
                                            final String username = repObj.get("twitter_id").getAsString();
                                            Ion.with(activity) // get bills
                                                    .load(String.format("http://congress.api.sunlightfoundation.com/bills?sponsor_id=%s&apikey=%s", bioID, SUNLIGHT_KEY))
                                                    .asJsonObject()
                                                    .setCallback(new FutureCallback<JsonObject>() {
                                                        @Override
                                                        public void onCompleted(Exception e, JsonObject billsResult) {
                                                            final ArrayList<Bill> bills = new ArrayList<>();
                                                            for (JsonElement item : billsResult.getAsJsonArray("results")) {
                                                                JsonObject obj = item.getAsJsonObject();
                                                                String name = obj.has("popular_title") && !obj.get("popular_title").isJsonNull() ? obj.get("popular_title").getAsString() : obj.get("official_title").getAsString();
                                                                bills.add(new Bill(
                                                                        name,
                                                                        item.getAsJsonObject().get("introduced_on").getAsString()
                                                                ));
                                                            }
                                                            Ion.with(activity) // get committees
                                                                    .load(String.format("http://congress.api.sunlightfoundation.com/committees?member_ids=%s&apikey=%s", bioID, SUNLIGHT_KEY))
                                                                    .asJsonObject()
                                                                    .setCallback(new FutureCallback<JsonObject>() {
                                                                        @Override
                                                                        public void onCompleted(Exception e, JsonObject committeesResult) {
                                                                            final ArrayList<Committee> committees = new ArrayList<>();
                                                                            for (JsonElement item : committeesResult.getAsJsonArray("results")) {
                                                                                String name = item.getAsJsonObject().get("name").getAsString();
                                                                                committees.add(new Committee(
                                                                                        name,
                                                                                        item.getAsJsonObject().get("committee_id").getAsString()
                                                                                ));
                                                                            }
                                                                            Ion.with(activity) // get Twitter profile
                                                                                    .load(String.format("https://api.twitter.com/1.1/users/show.json?screen_name=%s", username))
                                                                                    .setHeader("Authorization", "Bearer " + activity.twitterToken)
                                                                                    .asJsonObject()
                                                                                    .setCallback(new FutureCallback<JsonObject>() {
                                                                                        @Override
                                                                                        public void onCompleted(Exception e, JsonObject profile) {
                                                                                            Log.d(TAG, profile.toString());
                                                                                            String imgURL = profile.get("profile_image_url").getAsString();
                                                                                            int extensionDotIndex = imgURL.indexOf('.', imgURL.length() - 10); // look for extension separator near end of URL
                                                                                            String fullSizeURL = imgURL.substring(0, extensionDotIndex - 7) + imgURL.substring(extensionDotIndex);
                                                                                            String bannerURL = profile.has("profile_banner_url") ? profile.get("profile_banner_url").getAsString() : "";
                                                                                            activity.addRep(new UnloadedRepresentative(
                                                                                                    repObj.get("first_name").getAsString() + " " +
                                                                                                            repObj.get("last_name").getAsString(),
                                                                                                    Representative.RepType.fromString(repObj.get("title").getAsString()),
                                                                                                    repObj.get("state").getAsString(),
                                                                                                    Representative.Party.fromString(repObj.get("party").getAsString()),
                                                                                                    fullSizeURL,
                                                                                                    bannerURL,
                                                                                                    !(profile.get("status").getAsJsonObject().has("extended_entities") &&
                                                                                                            profile.get("status").getAsJsonObject().get("extended_entities").getAsJsonObject()
                                                                                                                    .has("media")) || bannerURL.equals(""),
                                                                                                    profile.get("status").getAsJsonObject().get("id").getAsLong(),
                                                                                                    repObj.get("website").getAsString(),
                                                                                                    repObj.get("oc_email").getAsString(),
                                                                                                    bills.toArray(new Bill[bills.size()]),
                                                                                                    committees.toArray(new Committee[committees.size()])
                                                                                            ));
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }

    public void randomLoc() {
        int index = (int) (Math.random() * this.locList.size());
        JsonObject loc = this.locList.get(index).getAsJsonObject();
        Intent restart = new Intent(activity, RepListActivity.class)
                .putExtra("lat", loc.get("lat").getAsDouble())
                .putExtra("long", loc.get("lng").getAsDouble())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(restart);
    }

    public void switchToRep(UnloadedRepresentative to, View view) {
        if (view == null) Log.d(TAG, "switchToRep: null view");
                activity.startActivity(
                        new Intent(activity, RepActivity.class)
                                .putExtra("rep", to.toBundle())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    RecyclerView.Adapter repListAdapter = new RecyclerView.Adapter<RepListViewHolder>() {
        @Override
        public RepListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final RepListViewHolder holder = new RepListViewHolder(LayoutInflater.from(parent.getContext())
                                                    .inflate(viewType, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final RepListViewHolder holder, int position) {
            activity.viewHolders.put(position, holder);

            final Resources resources = activity.getResources();
            final UnloadedRepresentative rep = activity.reps.get(position);

            if (position == 0) holder.itemView.setTop(8);

            holder.item = rep;
            holder.position = position;

            holder.itemView.setBackgroundColor(activity.getResources().getColor(rep.party.getColor()));

            if (rep.showBanner) {
                Ion.with(activity)
                        .load(rep.bannerURL)
                        .withBitmap()
                        .placeholder(R.drawable.default_rep_image)
                        .error(R.drawable.default_rep_image)
                        .intoImageView(holder.tile);
            }
            final int tweetStyle = rep.party == Representative.Party.dem ?
                    R.style.TweetDemStyle : rep.party == Representative.Party.rep ?
                    R.style.TweetRepStyle : R.style.TweetIndStyle;
            TweetUtils.loadTweet(rep.tweetID, new Callback<Tweet>() {
                @Override
                public void success(Result<Tweet> result) {
                    holder.tweetFrame.addView(new TweetView(activity, result.data, tweetStyle));

                }

                @Override
                public void failure(TwitterException e) {

                }
            });
            holder.name.setText(rep.name);
            holder.desc.setText(
                    String.format(getString(R.string.rep_desc_format), rep.type, rep.party, rep.state)
            );

            holder.itemView.findViewById(R.id.rep_web).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(rep.website));
                    activity.startActivity(i);
                }
            });
            holder.itemView.findViewById(R.id.rep_mail).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", rep.email, null));
                    activity.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return activity.reps.size();
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.rep_tile;
        }
    };

    class RepListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public UnloadedRepresentative item;
        private int position;
        public final View itemView;
        public final ImageView tile;

        public final TextView name;

        public final TextView desc;
        public final FrameLayout tweetFrame;
        public final View tweetBox;
        public final TextView username;
        public final TextView tweet;
        public final ImageButton expandButton;
        private final ImageButton webButton;
        private final ImageButton mailButton;

        public RepListViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            this.tile = (ImageView) itemView.findViewById(R.id.rep_tile);
            this.name = (TextView) itemView.findViewById(R.id.rep_name);
            this.desc = (TextView) itemView.findViewById(R.id.rep_desc);
            this.tweetFrame = (FrameLayout) itemView.findViewById(R.id.rep_tweet_frame);
            this.tweetBox = itemView.findViewById(R.id.rep_tweet_box);
            this.username = (TextView) itemView.findViewById(R.id.rep_username);
            this.tweet = (TextView) itemView.findViewById(R.id.rep_tweet);
            this.expandButton = (ImageButton) itemView.findViewById(R.id.rep_expand);
            this.webButton = (ImageButton) itemView.findViewById(R.id.rep_web);
            this.mailButton = (ImageButton) itemView.findViewById(R.id.rep_mail);

            this.expandButton.setOnClickListener(this);
            this.webButton.setOnClickListener(this);
            this.mailButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.rep_expand) {
                this.itemView.setTransitionName("tile");
                activity.switchToRep(this.item, this.itemView);
            } else if (v.getId() == R.id.rep_web) {
                // TODO: open website
            } else if (v.getId() == R.id.rep_mail) {
                // TODO: email
            }
        }
    }
}
