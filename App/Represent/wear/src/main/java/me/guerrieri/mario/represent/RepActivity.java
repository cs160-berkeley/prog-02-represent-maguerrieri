package me.guerrieri.mario.represent;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

public class RepActivity extends WearableActivity {
    private static final String TAG = "wear/RepActivity";
    private RepActivity activity = this;

    private GridViewPager pager;

    private String zip;
    private Representative[] reps;

//    private Uri photo;

    private GoogleApiClient apiClient;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private PresCardFragment presCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep);

        this.apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended");
                    }
                })
                .build();
        this.apiClient.connect();

        this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.accel = 0.00f;
        this.accelCurrent = SensorManager.GRAVITY_EARTH;
        this.accelLast = SensorManager.GRAVITY_EARTH;

        this.pager = ((GridViewPager) this.findViewById(R.id.grid_pager));
        this.pager.setAdapter(new RepCardFragmentAdapter(this.getFragmentManager()));
        this.pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, int i1, float v, float v1, int i2, int i3) {

            }

            @Override
            public void onPageSelected(final int row, final int col) {
                Log.d(TAG, String.format("changed to page %s", Integer.toString(col - 1)));
                if (col > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, String.format("sending %s %s", activity.getString(R.string.rep_changed_path), Integer.toString(col - 1)));
                            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(activity.apiClient).await();
                            for (Node node : nodes.getNodes()) {
                                //we find 'nodes', which are nearby bluetooth devices (aka emulators)
                                //send a message for each of these nodes (just one, for an emulator)
                                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                        activity.apiClient, node.getId(), getString(R.string.rep_changed_path), Integer.toString(col - 1).getBytes()).await();
                                //4 arguments: api client, the node ID, the path (for the listener to parse),
                                //and the message itself (you need to convert it to bytes.)
                                Log.d(TAG, String.format("message sent: %s", result.getStatus().getStatusMessage()));
                            }
                        }
                    }).start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
//        BitmapDrawable test = new BitmapDrawable(this.getResources(), BitmapFactory.decodeResource(this.getResources(), R.drawable.default_rep_image));
//        this.reps = new Representative[] {
//            new Representative(
//                    "test",
//                    Representative.RepType.rep,
//                    "CA",
//                    Representative.Party.dem,
//                    0,
//                    "@hello",
//                    "lol",
//                    new Bill[0],
//                    new Committee[0]
//            )
//        };
//        setAmbientEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, String.format("got rep change %s", intent.getExtras().getInt("ind")));
                activity.pager.setCurrentItem(0, intent.getExtras().getInt("ind") + 1);
            }
        }, new IntentFilter(this.getString(R.string.rep_changed_action)));

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.zip = extras.getString("zip");
            if (extras.getBundle("reps") != null) {
                Bundle repsBundle = extras.getBundle("reps");
                ArrayList<Representative> reps = new ArrayList<>();
                int i = 0;
                Bundle bundle = repsBundle.getBundle("0");
                while (bundle != null) {
                    reps.add(new Representative(bundle));
                    bundle = repsBundle.getBundle(Integer.toString(++i));
                }
                this.reps = reps.toArray(new Representative[reps.size()]);
            }
        }

        this.sensorManager.registerListener(new SensorEventListener() { // TODO: unregister on pause
            @Override
            public void onSensorChanged(SensorEvent se) {
                Log.d(TAG, String.format("onSensorChanged %s", Arrays.toString(se.values)));
                // http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it
                float x = se.values[0];
                float y = se.values[1];
                float z = se.values[2];
                activity.accelLast = activity.accelCurrent;
                activity.accelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
                float delta = activity.accelCurrent - activity.accelLast;
                activity.accel = activity.accel * 0.9f + delta; // perform low-cut filter

                if (activity.accel > 12) {
                    activity.sendRandom();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sendRandom() {
        this.zip = "99999";
        if (this.presCard != null) this.presCard.updateZip(this.zip);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, String.format("sending %s", activity.getString(R.string.rep_random_path)));
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(activity.apiClient).await();
                for (Node node : nodes.getNodes()) {
                    //we find 'nodes', which are nearby bluetooth devices (aka emulators)
                    //send a message for each of these nodes (just one, for an emulator)
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            activity.apiClient, node.getId(), getString(R.string.rep_random_path), "SHAKE SHAKE SHAKE".getBytes()).await();
                    //4 arguments: api client, the node ID, the path (for the listener to parse),
                    //and the message itself (you need to convert it to bytes.)
                    Log.d(TAG, String.format("message sent: %s", result.getStatus().getStatusMessage()));
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.apiClient.disconnect();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
//        if (isAmbient()) {
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//            mTextView.setTextColor(getResources().getColor(android.R.color.white));
//            mClockView.setVisibility(View.VISIBLE);
//
//            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
//        } else {
//            mContainerView.setBackground(null);
//            mTextView.setTextColor(getResources().getColor(android.R.color.black));
//            mClockView.setVisibility(View.GONE);
//        }
    }

    class RepCardFragmentAdapter extends FragmentGridPagerAdapter {
        public RepCardFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(int row, int col) {
            if (activity.reps != null && activity.reps.length > 0) {
                if (col > 0) {
                    Representative rep = activity.reps[col - 1];
                    Fragment frag = new RepCardFragment();
                    Bundle args = rep.toBundle();
                    if (activity.zip != null) args.putString("zip", activity.zip);
                    frag.setArguments(args);
                    return frag;
                } else {
                    activity.presCard = new PresCardFragment();
                    return activity.presCard;
                }
            } else return new NoZipCardFragment();
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int i) {
            if (activity.reps != null && activity.reps.length > 0) return activity.reps.length + 1;
            else return 1;
        }
    }
}
