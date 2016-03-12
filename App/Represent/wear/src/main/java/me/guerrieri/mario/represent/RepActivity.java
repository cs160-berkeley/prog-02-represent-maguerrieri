package me.guerrieri.mario.represent;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.FutureRunnable;
import com.koushikdutta.async.future.FutureThread;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

public class RepActivity extends WearableActivity {
    private static final String TAG = "wear/RepActivity";
    private RepActivity activity = this;

    private GridViewPager pager;

    private String zip;
    private ArrayList<Representative> reps;

//    private Uri photo;

    private GoogleApiClient apiClient;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private PresCardFragment presCard;
    private Node connectedNode;
    private RepCardFragmentAdapter adapter;
    private String county;
    private double obamaVotes;
    private double romneyVotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep);

        this.reps = new ArrayList<>();

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
        this.adapter = new RepCardFragmentAdapter(this.getFragmentManager());
        this.pager.setAdapter(this.adapter);
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
                            Log.d(TAG, String.format("sending %s %s", activity.getString(R.string.rep_changed_path), activity.reps.get(col - 1).name));
                            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(activity.apiClient).await();
                            for (Node node : nodes.getNodes()) {
                                //we find 'nodes', which are nearby bluetooth devices (aka emulators)
                                //send a message for each of these nodes (just one, for an emulator)
                                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                        activity.apiClient, node.getId(), getString(R.string.rep_changed_path), activity.reps.get(col - 1).name.getBytes()).await();
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

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(TAG, "rep changed");
                Wearable.NodeApi
                        .getConnectedNodes(activity.apiClient)
                        .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            activity.connectedNode = node;
                        }
                        Uri uri = new Uri.Builder()
                                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                                .path(intent.getStringExtra("dataPath"))
                                .authority(activity.connectedNode.getId())
                                .build();
                        Wearable.DataApi.getDataItem(activity.apiClient, uri)
                                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {
//                                        byte[] data = dataItemResult
//                                                .getDataItem()
//                                                .getData();
                                        final DataMap dataMap = DataMapItem.fromDataItem(dataItemResult
                                                .getDataItem()).getDataMap();
                                        if (dataMap.getString("dataType").equals("rep")) {
                                            for (int i = 0; dataMap.containsKey(Integer.toString(i)); i ++) {
                                                final DataMap innerMap = dataMap.getDataMap(Integer.toString(i));
                                                activity.loadBitmapFromAsset(innerMap.getAsset("photo")).setCallback(new FutureCallback<Bitmap>() {
                                                    @Override
                                                    public void onCompleted(Exception e, final Bitmap photoResult) {
                                                        activity.loadBitmapFromAsset(innerMap.getAsset("banner")).setCallback(new FutureCallback<Bitmap>() {
                                                            @Override
                                                            public void onCompleted(Exception e, Bitmap bannerResult) {
                                                                activity.addRep(
                                                                        innerMap.getInt("ind"),
                                                                        new Representative(
                                                                                innerMap.getString("name"),
                                                                                Representative.RepType.values()[innerMap.getInt("type")],
                                                                                innerMap.getString("state"),
                                                                                Representative.Party.values()[innerMap.getInt("party")],
                                                                                photoResult,
                                                                                bannerResult,
                                                                                null, null
                                                                        ));
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        } else {
                                            activity.reps.clear();
                                            activity.adapter.notifyDataSetChanged();
                                            activity.county = dataMap.getString("county");
                                            activity.obamaVotes = dataMap.getDouble("obama");
                                            activity.romneyVotes = dataMap.getDouble("romney");
                                            activity.adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });
            }
        }, new IntentFilter(this.getString(R.string.rep_changed_action)));
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

    private void addRep(int index, Representative representative) {
        this.reps.add(representative);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(TAG, String.format("got rep change %s", intent.getExtras().getInt("ind")));
//                activity.pager.setCurrentItem(0, intent.getExtras().getInt("ind") + 1);
//            }
//        }, new IntentFilter(this.getString(R.string.rep_changed_action)));

//        Bundle extras = this.getIntent().getExtras();
//        if (extras != null) {
//            this.zip = extras.getString("zip");
//            if (extras.getBundle("reps") != null) {
//                Bundle repsBundle = extras.getBundle("reps");
//                ArrayList<Representative> reps = new ArrayList<>();
//                int i = 0;
//                Bundle bundle = repsBundle.getBundle("0");
//                while (bundle != null) {
//                    reps.add(new Representative(bundle));
//                    bundle = repsBundle.getBundle(Integer.toString(++i));
//                }
//                this.reps = reps;
//            }
//        }

        this.sensorManager.registerListener(new SensorEventListener() { // TODO: unregister on pause
            @Override
            public void onSensorChanged(SensorEvent se) {
                // http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it
                float x = se.values[0];
                float y = se.values[1];
                float z = se.values[2];
                activity.accelLast = activity.accelCurrent;
                activity.accelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
                float delta = activity.accelCurrent - activity.accelLast;
                activity.accel = activity.accel * 0.9f + delta; // perform low-cut filter

                if (activity.accel > 12) {
                    Log.d(TAG, String.format("onSensorChanged %s", Arrays.toString(se.values)));
                    activity.sendRandom();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sendRandom() {
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

    private com.koushikdutta.async.future.Future<Bitmap> loadBitmapFromAsset(final Asset asset) {
        return new FutureThread<>(new FutureRunnable<Bitmap>() {
            @Override
            public Bitmap run() throws Exception {
                InputStream assetInputStream = Wearable.DataApi
                        .getFdForAsset(activity.apiClient, asset)
                        .await()
                        .getInputStream();
                return BitmapFactory.decodeStream(assetInputStream);
            }
        });
    }

    public Representative getRep(int index) {
        return this.reps.get(index);
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
            if (activity.reps != null && activity.reps.size() > 0) {
                if (col > 0) {
                    Representative rep = activity.reps.get(col - 1);
                    Fragment frag = new RepCardFragment();
                    Bundle args = rep.toBundle();
                    args.putInt("ind", col - 1);
                    frag.setArguments(args);
                    return frag;
                } else {
                    activity.presCard = new PresCardFragment();
                    Bundle args = new Bundle();
                    args.putString("county", activity.county);
                    args.putDouble("obama", activity.obamaVotes);
                    args.putDouble("romney", activity.romneyVotes);
                    activity.presCard.setArguments(args);
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
            if (activity.reps != null && activity.reps.size() > 0) return activity.reps.size() + 1;
            else return 1;
        }
    }
}
